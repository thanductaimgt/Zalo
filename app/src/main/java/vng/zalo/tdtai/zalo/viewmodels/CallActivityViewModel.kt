package vng.zalo.tdtai.zalo.viewmodels

import android.content.Context
import android.content.Intent
import android.net.sip.SipAudioCall
import android.net.sip.SipProfile
import android.net.sip.SipSession
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.MessageManager
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomPeer
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils


class CallActivityViewModel(val intent: Intent, listener:SipAudioCall.Listener) : ViewModel() {
    lateinit var sipAudioCall: SipAudioCall

    var isExternalSpeakerEnabled = false
    var isRecorderEnabled = true
    var startTime: Long = 0
    var isCaller = false

    private val room: RoomPeer = RoomPeer(
            avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
            id = intent.getStringExtra(Constants.ROOM_ID),
            name = intent.getStringExtra(Constants.ROOM_NAME),
            phone = intent.getStringExtra(Constants.ROOM_PHONE)
    )

    private val peerPhone = room.phone

    val liveCallState = MutableLiveData<Int>(SipSession.State.READY_TO_CALL)

    init {
        isCaller = intent.getBooleanExtra(Constants.IS_CALLER, false)

        if(isCaller){
            //get current room info
            Database.getRoomInfo(room.id!!) {
                room.createdTime = it.createdTime
                room.memberMap = it.memberMap
            }

            makeAudioCall(listener)
        } else {
            takeAudioCall(listener)
        }
    }

    private fun makeAudioCall(listener:SipAudioCall.Listener){
        val peerUserName = "${Constants.SIP_ACCOUNT_PREFIX}$peerPhone"
        val peerDomain = Constants.SIP_DOMAIN

        val peerSipProfile = SipProfile.Builder(peerUserName, peerDomain).build()

        Log.d(TAG, "calling $peerUserName")

        sipAudioCall = ZaloApplication.sipManager!!.makeAudioCall(
                ZaloApplication.sipProfile!!.uriString,
                peerSipProfile.uriString,
                listener,
                20
        )
    }

    private fun takeAudioCall(listener:SipAudioCall.Listener){
        sipAudioCall = ZaloApplication.sipManager!!.takeAudioCall(intent, null)
        sipAudioCall.setListener(listener, true)
    }

    fun getPeerPhone():String{
        return Utils.getPhoneFromSipProfileName(sipAudioCall.peerProfile.userName)
    }

    fun addNewCallMessage(context: Context, messageType:Int, callType:Int, callTime:Int, isMissed:Boolean, isCancel:Boolean=false) {
        val contents = ArrayList<String>().apply {
            add("$callType.$callTime.$isMissed.$isCancel")
        }
        MessageManager.addNewMessagesToFirestore(context, room, contents, messageType)
    }
}