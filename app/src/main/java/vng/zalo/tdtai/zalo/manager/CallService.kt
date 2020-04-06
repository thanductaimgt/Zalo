package vng.zalo.tdtai.zalo.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.sip.SipAudioCall
import android.net.sip.SipManager
import android.net.sip.SipProfile
import android.net.sip.SipRegistrationListener
import android.os.Handler
import android.util.Log
import dagger.Lazy
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.receiver.CallReceiver
import vng.zalo.tdtai.zalo.ui.call.CallActivity
import vng.zalo.tdtai.zalo.util.Constants
import vng.zalo.tdtai.zalo.util.TAG
import javax.inject.Inject

interface CallService {
    fun init()

    fun stop()

    fun isInit(): Boolean

    fun startCallActivity(context: Context, roomId: String, roomName: String, roomPhone: String, roomAvatarUrl: String) {
        context.startActivity(Intent(context, CallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)

            putExtra(Constants.IS_CALLER, true)

            putExtra(Constants.ROOM_ID, roomId)
            putExtra(Constants.ROOM_NAME, roomName)
            putExtra(Constants.ROOM_PHONE, roomPhone)
            putExtra(Constants.ROOM_AVATAR, roomAvatarUrl)
        })
    }

    fun makeAudioCall(targetPhone: String, listenerAudio: AudioCallListener): AudioCall

    fun takeAudioCall(intent: Intent, listenerAudio: AudioCallListener): AudioCall

    interface AudioCall {
        fun toggleMute()

        fun answerCall(timeout: Int)

        fun endCall()

        fun isInCall(): Boolean

        fun getPeerPhone(): String

        fun startAudio()

        fun setSpeakerMode(isEnabled: Boolean)

        fun isMuted(): Boolean

        fun close()
    }

    interface AudioCallListener {
        fun onCallEstablished()

        fun onCallEnded()

        fun onCallBusy()

        fun onCalling()

        fun onError(errorCode: Int?, errorMessage: String?)

        fun onRingingBack()
    }
}

class SipCallService @Inject constructor(
        private val application: ZaloApplication,
        private val lazySessionManager: Lazy<SessionManager>
) : CallService {
    private val callReceiver: CallReceiver = CallReceiver()
    private val filter = IntentFilter().apply {
        addAction(Constants.ACTION_CALL)
    }

    private var sipManager: SipManager? = null
    private var sipProfile: SipProfile? = null
    private var sipListener: SipAudioCall.Listener? = null

    private fun registerCallReceiver() {
        application.registerReceiver(callReceiver, filter)
    }

    override fun init() {
        sipProfile = SipProfile.Builder("${SIP_ACCOUNT_PREFIX}${lazySessionManager.get().curUser!!.id}", SIP_DOMAIN)
                .setPassword(lazySessionManager.get().curUser!!.id)
//                    .setAutoRegistration(true)
                .build()

        sipManager = SipManager.newInstance(application)

        val intent = Intent(Constants.ACTION_CALL)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(application, 0, intent, Intent.FILL_IN_DATA)

        openSip(pendingIntent)

        registerCallReceiver()
    }

    override fun stop() {
        removeSip()
        unregisterCallReceiver()
    }

    override fun isInit(): Boolean {
        return sipManager != null
    }

    override fun makeAudioCall(targetPhone: String, listenerAudio: CallService.AudioCallListener): CallService.AudioCall {
        val peerUserName = "$SIP_ACCOUNT_PREFIX$targetPhone"
        val peerDomain = SIP_DOMAIN

        val peerSipProfile = SipProfile.Builder(peerUserName, peerDomain).build()

        Log.d(TAG, "calling $peerUserName")

        sipListener = SipAudioCallListener(listenerAudio)

        return AudioCallImpl(
                sipManager!!.makeAudioCall(
                        sipProfile!!.uriString,
                        peerSipProfile.uriString,
                        sipListener,
                        20
                )
        )
    }

    override fun takeAudioCall(intent: Intent, listenerAudio: CallService.AudioCallListener): CallService.AudioCall {
        sipListener = SipAudioCallListener(listenerAudio)

        return AudioCallImpl(
                sipManager!!.takeAudioCall(intent, sipListener)
        )
//        sipAudioCall!!.setListener(listener, true)
    }

    private fun openSip(pendingIntent: PendingIntent, retryOnFail: Boolean = true) {
        try {
            sipManager?.open(sipProfile, pendingIntent, object : SipRegistrationListener {
                override fun onRegistering(localProfileUri: String?) {
                    Log.d(TAG, "open: onRegistering")
                }

                override fun onRegistrationDone(localProfileUri: String?, expiryTime: Long) {
                    Log.d(TAG, "open: onRegistrationDone")
                }

                override fun onRegistrationFailed(localProfileUri: String?, errorCode: Int, errorMessage: String?) {
                    Log.d(TAG, "open: onRegistrationFailed")
                }
            })
        } catch (t: Throwable) {
            if (retryOnFail) {
                Handler().postDelayed({
                    openSip(pendingIntent)
                }, 10000)
            }

            t.printStackTrace()
        }
    }

    private fun removeSip() {
        try {
            sipManager?.close(sipProfile?.uriString)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to close local profile")
            t.printStackTrace()
        }
    }

    private fun unregisterCallReceiver() {
        sipManager?.let { application.unregisterReceiver(callReceiver) }
    }

    private inner class AudioCallImpl(private val sipAudioCall: SipAudioCall) : CallService.AudioCall {
        override fun toggleMute() {
            sipAudioCall.toggleMute()
        }

        override fun answerCall(timeout: Int) {
            sipAudioCall.answerCall(timeout)
        }

        override fun endCall() {
            sipAudioCall.endCall()
        }

        override fun isInCall(): Boolean {
            return sipAudioCall.isInCall
        }


        override fun getPeerPhone(): String {
            val sipUserName = sipAudioCall.peerProfile.userName
            return sipUserName.takeLastWhile { it != '.' }
        }

        override fun startAudio() {
            sipAudioCall.startAudio()
        }

        override fun setSpeakerMode(isEnabled: Boolean) {
            sipAudioCall.setSpeakerMode(isEnabled)
        }

        override fun isMuted(): Boolean {
            return sipAudioCall.isMuted
        }

        override fun close() {
            sipAudioCall.close()
        }
    }

    class SipAudioCallListener @Inject constructor(private val listenerAudio: CallService.AudioCallListener) : SipAudioCall.Listener() {
        override fun onCallEstablished(call: SipAudioCall) {
            listenerAudio.onCallEstablished()
        }

        //me/other cancel/finish call
        override fun onCallEnded(call: SipAudioCall) {
            listenerAudio.onCallEnded()
        }

//        override fun onRinging(call: SipAudioCall, caller: SipProfile) {
//            0
//        }

        //other cancel/not answer
        override fun onCallBusy(call: SipAudioCall?) {
            listenerAudio.onCallBusy()
        }

//        override fun onCallHeld(call: SipAudioCall?) {
//            0
//        }

        //sent call signal
        override fun onCalling(call: SipAudioCall?) {
            listenerAudio.onCalling()
        }

        override fun onError(call: SipAudioCall?, errorCode: Int, errorMessage: String?) {
            listenerAudio.onError(errorCode, errorMessage)
        }

//        override fun onReadyToCall(call: SipAudioCall?) {
//            0
//        }

        //when other is ringing
        override fun onRingingBack(call: SipAudioCall?) {
            listenerAudio.onRingingBack()
        }
    }

    companion object {
        const val SIP_DOMAIN = "sip.linphone.org"
        const val SIP_ACCOUNT_PREFIX = "zalo.vng.tdtai.zalo.user."
    }
}