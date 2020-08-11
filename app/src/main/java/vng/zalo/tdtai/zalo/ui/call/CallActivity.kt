package vng.zalo.tdtai.zalo.ui.call

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_call.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.data_model.message.CallMessage
import vng.zalo.tdtai.zalo.manager.CallService
import vng.zalo.tdtai.zalo.util.Constants
import vng.zalo.tdtai.zalo.util.smartLoad
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


class CallActivity : BaseActivity() {
    private val viewModel: CallViewModel by viewModels { viewModelFactory }

    val audioCallListener = AudioCallListener()
    private lateinit var audioManager: AudioManager
    private var keyguardManager: KeyguardManager? = null

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

    override fun initAll() {
        try {
            onBindViews()
            onViewsBound()
        } catch (e: Exception) {
            alertDialog.show(supportFragmentManager,
                    getString(R.string.label_error_occurred),
                    "${getString(R.string.label_error)}: ${e.message}",
                    button1Action = { finish() })

            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBindViews() {
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

        setContentView(R.layout.activity_call, true)

        if (viewModel.isCaller) {
            nameTextView.text = viewModel.liveRoom.value!!.getDisplayName(resourceManager)
            Picasso.get().smartLoad(viewModel.liveRoom.value!!.avatarUrl, resourceManager, watchOwnerAvatarImgView) {
                it.fit().centerCrop()
            }
        } else {
            statusTextView.text = getString(R.string.description_incoming_call)

            answerImgView.visibility = View.VISIBLE
            answerImgView.setOnClickListener(this)
        }

        speakerImgView.setOnClickListener(this)
        cancelCallImgView.setOnClickListener(this)
        recorderImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
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

        viewModel.liveRoom.observe(this, Observer { roomPeer ->
            nameTextView.text = roomPeer.getDisplayName(resourceManager)
            Picasso.get().smartLoad(roomPeer.avatarUrl, resourceManager, watchOwnerAvatarImgView) {
                it.fit().centerCrop()
            }
        })

        initAudioManager()

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
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

                viewModel.audioCall.toggleMute()

                viewModel.isRecorderEnabled = !viewModel.isRecorderEnabled
            }
            R.id.answerImgView -> {
                viewModel.audioCall.answerCall(10)
            }
            R.id.cancelCallImgView -> {
                viewModel.audioCall.endCall()
                if (!viewModel.audioCall.isInCall()) {
                    finish()
                }
            }
            R.id.backImgView -> {
                if (viewModel.audioCall.isInCall()) {
                    alertDialog.show(supportFragmentManager,
                            title = getString(R.string.label_end_call),
                            description = getString(R.string.description_end_call),
                            button1Text = getString(R.string.label_cancel),
                            button2Text = getString(R.string.label_ok),
                            button2Action = { viewModel.audioCall.endCall() })
                } else {
                    finish()
//                    viewModel.sipAudioCall.endCall()
                }
            }
        }
    }

    inner class AudioCallListener : CallService.AudioCallListener {
        override fun onCallEstablished() {
            viewModel.liveCallState.postValue(STATE_ESTABLISHED)

            viewModel.audioCall.apply {
                startAudio()
                setSpeakerMode(true)
                if (isMuted()) {
                    toggleMute()
                }
                volumeControlStream = AudioManager.STREAM_VOICE_CALL
            }

            viewModel.startTime = System.currentTimeMillis()
        }

        //me/other cancel/finish call
        override fun onCallEnded() {
            handler.removeCallbacks(timerRunnable)

            handler.apply {
                postDelayed({
                    finish()
                }, 2000)
            }

            if (viewModel.isCaller) {
                val isMissed: Boolean
                val callTime: Int
                if (viewModel.liveCallState.value != STATE_ESTABLISHED) {
                    isMissed = true
                    callTime = 0
                } else {
                    isMissed = false
                    callTime = ((System.currentTimeMillis() - viewModel.startTime) / Constants.ONE_SECOND_IN_MILLISECOND).toInt()
                }

                viewModel.addNewCallMessage(
                        CallMessage.CALL_TYPE_VOICE,
                        callTime,
                        isMissed
                )
            }

            viewModel.liveCallState.postValue(STATE_ENDED)
        }

//        override fun onRinging(call: SipAudioCall, caller: SipProfile) {
//            0
//        }

        //other cancel/not answer
        override fun onCallBusy() {
            viewModel.liveCallState.postValue(STATE_BUSY)

            if (viewModel.isCaller) {
                viewModel.addNewCallMessage(
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
        override fun onCalling() {
            viewModel.liveCallState.postValue(STATE_CALLING)
        }

        override fun onError(errorCode: Int?, errorMessage: String?) {
            handler.post {
                alertDialog.show(supportFragmentManager,
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
        override fun onRingingBack() {
            viewModel.liveCallState.postValue(STATE_RINGING_BACK)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            viewModel.audioCall.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    companion object {
        const val STATE_CALLING = 0
        const val STATE_RINGING_BACK = 1
        const val STATE_BUSY = 2
        const val STATE_ENDED = 3
        const val STATE_ESTABLISHED = 4
//        const val STATE_INIT = 5
    }
}
