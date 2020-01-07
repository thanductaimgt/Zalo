package vng.zalo.tdtai.zalo.views.activities

import android.content.Context
import android.media.AudioManager
import android.net.sip.SipAudioCall
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_call.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.models.message.CallMessage
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.viewmodels.CallActivityViewModel
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


class CallActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: CallActivityViewModel
    private lateinit var audioManager: AudioManager

    private var listener = SipAudioCallListener()

    private var isExternalSpeakerEnabled = false
    private var isRecorderEnabled = true

    private var isCaller = false

    private var speakerMaxVolume = 0
    private var speakerMinVolume = 0
    private var externalSpeakerIncreaseVolume = 0

    var startTime: Long = 0

    private val handler = Handler()
    private var timerRunnable = object : Runnable {
        override fun run() {
            val millis = System.currentTimeMillis() - startTime
            var seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            seconds %= 60

            timeTextView.text = String.format("%02d:%02d", minutes, seconds)

            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCaller = intent.getBooleanExtra(Constants.IS_CALLER, false)

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(intent = intent)).get(CallActivityViewModel::class.java)

        try {
            if (isCaller) {
                viewModel.makeAudioCall(listener)
            } else {
                viewModel.takeAudioCall(listener)
            }
        } catch (e: Exception) {
            ZaloApplication.notificationDialog.show(supportFragmentManager,
                    getString(R.string.label_error_occurred),
                    "${getString(R.string.label_error)}: ${e.message}",
                    button1Action = { finish() })

            e.printStackTrace()
        }

        initAudioManager()

        initView()
    }

    private fun initAudioManager() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        speakerMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        speakerMinVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_VOICE_CALL)
        } else {
            0
        }

        externalSpeakerIncreaseVolume = ceil((speakerMaxVolume - speakerMinVolume) / 2f).toInt()
    }

    public override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerRunnable)
    }

    private fun initView() {
        setContentView(R.layout.activity_call)

        if (isCaller) {
            answerImgView.visibility = View.GONE

            nameTextView.text = intent.getStringExtra(Constants.ROOM_NAME)
            Picasso.get().load(intent.getStringExtra(Constants.ROOM_AVATAR)).fit().centerInside().into(avatarImgView)
        } else {
            statusTextView.text = getString(R.string.description_incoming_call)

            answerImgView.setOnClickListener(this)

            val peerPhone = viewModel.getPeerPhone()
            nameTextView.text = peerPhone
            Database.getUserRoom(peerPhone) { roomItem ->
                Picasso.get().load(roomItem.avatarUrl).fit().centerInside().into(avatarImgView)
                Log.d(TAG, "avatarUrl: ${roomItem.avatarUrl}")
            }
        }

        speakerImgView.setOnClickListener(this)
        cancelCallImgView.setOnClickListener(this)
        recorderImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.speakerImgView -> {
                val curVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)

                val volumeToSet = if (isExternalSpeakerEnabled) {
                    speakerImgView.setImageResource(R.drawable.internal_speaker)

                    Toast.makeText(this, getString(R.string.description_disabled_external_speaker), Toast.LENGTH_SHORT).show()

                    max(speakerMinVolume, curVolume - externalSpeakerIncreaseVolume)
                } else {
                    speakerImgView.setImageResource(R.drawable.external_speaker)

                    Toast.makeText(this, getString(R.string.description_using_external_speaker), Toast.LENGTH_SHORT).show()

                    min(speakerMaxVolume, curVolume + externalSpeakerIncreaseVolume)
                }

                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volumeToSet, AudioManager.FLAG_PLAY_SOUND)

                isExternalSpeakerEnabled = !isExternalSpeakerEnabled
            }
            R.id.recorderImgView -> {
                if (isRecorderEnabled) {
                    recorderImgView.setImageResource(R.drawable.muted_recorder_red)

                    Toast.makeText(this, getString(R.string.description_muted_speaker), Toast.LENGTH_SHORT).show()
                } else {
                    recorderImgView.setImageResource(R.drawable.muted_recorder_white)

                    Toast.makeText(this, getString(R.string.description_un_muted_speaker), Toast.LENGTH_SHORT).show()
                }

                viewModel.sipAudioCall.toggleMute()

                isRecorderEnabled = !isRecorderEnabled
            }
            R.id.answerImgView -> {
                viewModel.sipAudioCall.answerCall(10)
            }
            R.id.cancelCallImgView -> {
                if (viewModel.sipAudioCall.isInCall || !isCaller) {
                    viewModel.sipAudioCall.endCall()
                } else {
                    finish()
                }
            }
            R.id.backImgView -> {
                if (viewModel.sipAudioCall.isInCall) {
                    ZaloApplication.notificationDialog.show(supportFragmentManager,
                            title = getString(R.string.label_end_call),
                            description = getString(R.string.description_end_call),
                            button1Text = getString(R.string.label_cancel),
                            button2Text = getString(R.string.label_ok),
                            button2Action = { viewModel.sipAudioCall.endCall() })
                } else {
                    finish()
//                    viewModel.sipAudioCall.endCall()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sipAudioCall.close()
    }

    inner class SipAudioCallListener : SipAudioCall.Listener() {
        override fun onCallEstablished(call: SipAudioCall) {
            handler.post {
                timeIcon.visibility = View.VISIBLE
                timeTextView.visibility = View.VISIBLE
                signalIcon.visibility = View.VISIBLE
                signalTextView.visibility = View.VISIBLE

                statusTextView.visibility = View.GONE

                speakerImgView.visibility = View.VISIBLE
                recorderImgView.visibility = View.VISIBLE
                answerImgView.visibility = View.GONE
            }

            call.apply {
                startAudio()
                setSpeakerMode(true)
                if (isMuted) {
                    toggleMute()
                }
                volumeControlStream = AudioManager.STREAM_VOICE_CALL
            }

            startTime = System.currentTimeMillis()
            handler.postDelayed(timerRunnable, 0)
        }

        //me/other cancel/finish call
        override fun onCallEnded(call: SipAudioCall) {
            val callTime = ((System.currentTimeMillis() - startTime) / Constants.ONE_SECOND_IN_MILLISECOND).toInt()

            handler.removeCallbacks(timerRunnable)

            handler.apply {
                post {
                    signalIcon.visibility = View.GONE
                    signalTextView.visibility = View.GONE

                    speakerImgView.visibility = View.GONE
                    recorderImgView.visibility = View.GONE
                    cancelCallImgView.visibility = View.GONE
                    answerImgView.visibility = View.GONE

                    statusTextView.text = getString(R.string.description_call_ended)
                    statusTextView.visibility = View.VISIBLE

                    timeIcon.setImageResource(R.drawable.answer)
                }
                postDelayed({
                    finish()
                }, 2000)
            }

            if (isCaller) {
                viewModel.addNewCallMessage(
                        this@CallActivity,
                        Message.TYPE_CALL,
                        CallMessage.CALL_TYPE_VOICE,
                        callTime,
                        false
                )
            }
        }

//        override fun onRinging(call: SipAudioCall, caller: SipProfile) {
//            0
//        }

        //other cancel/not answer
        override fun onCallBusy(call: SipAudioCall?) {
            handler.apply {
                post {
                    statusTextView.text = getString(R.string.description_not_answer)
                    cancelCallImgView.visibility = View.INVISIBLE
                }
                postDelayed({
                    finish()
                }, 2000)
            }

            if (isCaller) {
                viewModel.addNewCallMessage(
                        this@CallActivity,
                        Message.TYPE_CALL,
                        CallMessage.CALL_TYPE_VOICE,
                        0,
                        true
                )
            }
        }

//        override fun onCallHeld(call: SipAudioCall?) {
//            0
//        }

        //sent call signal
        override fun onCalling(call: SipAudioCall?) {
            handler.post {
                statusTextView.text = getString(R.string.description_calling)
            }
        }

        override fun onError(call: SipAudioCall?, errorCode: Int, errorMessage: String?) {
            handler.post {
                ZaloApplication.notificationDialog.show(supportFragmentManager,
                        title = getString(R.string.label_error_occurred),
                        description = String.format(
                                "${getString(R.string.label_error)}: %s\n${getString(R.string.label_error_code)}: %d",
                                errorMessage, errorCode),
                        button1Action = {
                            finish()
                        })
            }
        }

//        override fun onReadyToCall(call: SipAudioCall?) {
//            0
//        }

        //when other is ringing
        override fun onRingingBack(call: SipAudioCall?) {
            handler.post {
                statusTextView.text = getString(R.string.description_ringing)
            }
        }
    }
}
