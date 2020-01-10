package vng.zalo.tdtai.zalo.views.activities

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioManager
import android.net.sip.SipAudioCall
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_call.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.message.CallMessage
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.viewmodels.CallActivityViewModel
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


class CallActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: CallActivityViewModel
    private lateinit var audioManager: AudioManager
    private var keyguardManager: KeyguardManager?=null

    private var listener = SipAudioCallListener()

    private var speakerMaxVolume = 0
    private var speakerMinVolume = 0
    private var externalSpeakerIncreaseVolume = 0

    private val handler = Handler()
    private var timerRunnable = object : Runnable {
        override fun run() {
            val millis = System.currentTimeMillis() - viewModel.startTime
            var seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            seconds %= 60

            timeTextView.text = String.format("%02d:%02d", minutes, seconds)

            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(intent = intent, sipAudioCallListener = listener)).get(CallActivityViewModel::class.java)

            initView()

            viewModel.liveCallState.observe(this, Observer {
                when (it) {
                    STATE_ESTABLISHED -> {
                        timeIcon.visibility = View.VISIBLE
                        timeTextView.visibility = View.VISIBLE
                        signalIcon.visibility = View.VISIBLE
                        signalTextView.visibility = View.VISIBLE

                        statusTextView.visibility = View.GONE

                        speakerImgView.visibility = View.VISIBLE
                        recorderImgView.visibility = View.VISIBLE
                        answerImgView.visibility = View.GONE

                        handler.postDelayed(timerRunnable, 0)
                    }
                    STATE_CALLING -> statusTextView.text = getString(R.string.description_calling)
                    STATE_RINGING_BACK -> statusTextView.text = getString(R.string.description_ringing)
                    STATE_BUSY -> {
                        statusTextView.text = getString(R.string.description_not_answer)
                        cancelCallImgView.visibility = View.INVISIBLE
                    }
                    STATE_ENDED -> {
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
                }
            })
        } catch (e: Exception) {
            ZaloApplication.notificationDialog.show(supportFragmentManager,
                    getString(R.string.label_error_occurred),
                    "${getString(R.string.label_error)}: ${e.message}",
                    button1Action = { finish() })

            e.printStackTrace()
        }

        initAudioManager()

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    override fun onStart() {
        super.onStart()

        // show activity if screen is locked or turn off
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    setShowWhenLocked(true)
                    setTurnScreenOn(true)
                } else {
                    addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                }

                keyguardManager?.requestDismissKeyguard(this@CallActivity, null)
            } else {
                addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            viewModel.sipAudioCall.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
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

    private fun initView() {
        setContentView(R.layout.activity_call)

        if (viewModel.isCaller) {
            nameTextView.text = viewModel.intent.getStringExtra(Constants.ROOM_NAME)
            Picasso.get().load(viewModel.intent.getStringExtra(Constants.ROOM_AVATAR)).fit().centerInside().into(avatarImgView)
        } else {
            answerImgView.visibility = View.VISIBLE
            statusTextView.text = getString(R.string.description_incoming_call)

            val peerPhone = viewModel.getPeerPhone()
            nameTextView.text = peerPhone
            Database.getUserRoom(peerPhone) { roomItem ->
                Picasso.get().load(roomItem.avatarUrl).fit().centerInside().into(avatarImgView)
            }

            answerImgView.setOnClickListener(this)
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

                val volumeToSet = if (viewModel.isExternalSpeakerEnabled) {
                    speakerImgView.setImageResource(R.drawable.internal_speaker)

                    Toast.makeText(this, getString(R.string.description_disabled_external_speaker), Toast.LENGTH_SHORT).show()

                    max(speakerMinVolume, curVolume - externalSpeakerIncreaseVolume)
                } else {
                    speakerImgView.setImageResource(R.drawable.external_speaker)

                    Toast.makeText(this, getString(R.string.description_using_external_speaker), Toast.LENGTH_SHORT).show()

                    min(speakerMaxVolume, curVolume + externalSpeakerIncreaseVolume)
                }

                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volumeToSet, AudioManager.FLAG_PLAY_SOUND)

                viewModel.isExternalSpeakerEnabled = !viewModel.isExternalSpeakerEnabled
            }
            R.id.recorderImgView -> {
                if (viewModel.isRecorderEnabled) {
                    recorderImgView.setImageResource(R.drawable.muted_recorder_red)

                    Toast.makeText(this, getString(R.string.description_muted_speaker), Toast.LENGTH_SHORT).show()
                } else {
                    recorderImgView.setImageResource(R.drawable.muted_recorder_white)

                    Toast.makeText(this, getString(R.string.description_un_muted_speaker), Toast.LENGTH_SHORT).show()
                }

                viewModel.sipAudioCall.toggleMute()

                viewModel.isRecorderEnabled = !viewModel.isRecorderEnabled
            }
            R.id.answerImgView -> {
                viewModel.sipAudioCall.answerCall(10)
            }
            R.id.cancelCallImgView -> {
                viewModel.sipAudioCall.endCall()
                if (!viewModel.sipAudioCall.isInCall) {
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

    inner class SipAudioCallListener : SipAudioCall.Listener() {
        override fun onCallEstablished(call: SipAudioCall) {
            viewModel.liveCallState.postValue(STATE_ESTABLISHED)

            call.apply {
                startAudio()
                setSpeakerMode(true)
                if (isMuted) {
                    toggleMute()
                }
                volumeControlStream = AudioManager.STREAM_VOICE_CALL
            }

            viewModel.startTime = System.currentTimeMillis()
        }

        //me/other cancel/finish call
        override fun onCallEnded(call: SipAudioCall) {
            viewModel.liveCallState.postValue(STATE_ENDED)

            val callTime = ((System.currentTimeMillis() - viewModel.startTime) / Constants.ONE_SECOND_IN_MILLISECOND).toInt()

            handler.removeCallbacks(timerRunnable)

            handler.apply {
                postDelayed({
                    finish()
                }, 2000)
            }

            if (viewModel.isCaller) {
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
            viewModel.liveCallState.postValue(STATE_BUSY)

            if (viewModel.isCaller) {
                viewModel.addNewCallMessage(
                        this@CallActivity,
                        Message.TYPE_CALL,
                        CallMessage.CALL_TYPE_VOICE,
                        0,
                        true
                )
            }

            handler.apply {
                postDelayed({
                    finish()
                }, 2000)
            }
        }

//        override fun onCallHeld(call: SipAudioCall?) {
//            0
//        }

        //sent call signal
        override fun onCalling(call: SipAudioCall?) {
            viewModel.liveCallState.postValue(STATE_CALLING)
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
            viewModel.liveCallState.postValue(STATE_RINGING_BACK)
        }
    }

    companion object {
        const val STATE_CALLING = 0
        const val STATE_RINGING_BACK = 1
        const val STATE_BUSY = 2
        const val STATE_ENDED = 3
        const val STATE_ESTABLISHED = 4
        const val STATE_INIT = 5
    }
}
