package vng.zalo.tdtai.zalo.viewmodels

import android.content.Context
import android.content.Intent
import android.net.sip.SipAudioCall
import android.net.sip.SipProfile
import android.util.Log
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.MessageCreator
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils


class CallActivityViewModel(private val intent: Intent) : ViewModel() {
    lateinit var sipAudioCall: SipAudioCall

    private val messageCreator = MessageCreator()

    private val room: Room = Room(
            avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
            id = intent.getStringExtra(Constants.ROOM_ID),
            name = intent.getStringExtra(Constants.ROOM_NAME)
    )

    private val peerPhone = room.name

    init {
        val isCaller = intent.getBooleanExtra(Constants.IS_CALLER, false)

        if(isCaller){
            //get current room info
            Database.getRoomInfo(room.id!!) {
                room.createdTime = it.createdTime
                room.memberMap = it.memberMap
            }
        }
    }

    fun makeAudioCall(listener:SipAudioCall.Listener){
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

    fun takeAudioCall(listener:SipAudioCall.Listener){
        sipAudioCall = ZaloApplication.sipManager!!.takeAudioCall(intent, listener)
    }

    fun getPeerPhone():String{
        return Utils.getPhoneFromSipProfileName(sipAudioCall.peerProfile.userName)
    }

    fun addNewCallMessage(context: Context, messageType:Int, callType:Int, callTime:Int, isMissed:Boolean, isCancel:Boolean=false) {
        val contents = ArrayList<String>().apply {
            add("$callType.$callTime.$isMissed.$isCancel")
        }
        messageCreator.addNewMessagesToFirestore(context, room, contents, messageType)
    }
}