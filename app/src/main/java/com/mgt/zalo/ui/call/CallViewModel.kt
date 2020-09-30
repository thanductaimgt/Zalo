package com.mgt.zalo.ui.call

import android.content.Context
import android.content.Intent
import android.net.sip.SipSession
import androidx.lifecycle.MutableLiveData
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.manager.CallService
import com.mgt.zalo.data_model.message.Message
import com.mgt.zalo.data_model.room.RoomPeer
import com.mgt.zalo.util.Constants
import javax.inject.Inject


class CallViewModel @Inject constructor(
        intent: Intent,
        listenerAudio:CallService.AudioCallListener
) : BaseViewModel() {
    var isExternalSpeakerEnabled = false
    var isRecorderEnabled = true
    var startTime: Long = 0
    var isCaller = false

    var doNeedAddMessage = false
    lateinit var contents:List<String>
    lateinit var context: Context

    val liveRoom = MutableLiveData<RoomPeer>()

    val liveCallState = MutableLiveData(SipSession.State.READY_TO_CALL)

    val audioCall:CallService.AudioCall

    init {
        isCaller = intent.getBooleanExtra(Constants.IS_CALLER, false)

        if(isCaller){
            liveRoom.value = RoomPeer(
                    avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
                    id = intent.getStringExtra(Constants.ROOM_ID),
                    name = intent.getStringExtra(Constants.ROOM_NAME),
                    peerId = intent.getStringExtra(Constants.ROOM_PHONE)
            )

            //get current room info
            database.getRoomInfo(liveRoom.value!!.id!!) {
                liveRoom.value!!.createdTime = it.createdTime
                liveRoom.value!!.memberMap = it.memberMap

                if(doNeedAddMessage){
                    messageManager.addNewMessagesToFirestore(liveRoom.value!!, contents, Message.TYPE_CALL)
                }
            }

            audioCall = callService.makeAudioCall(liveRoom.value!!.peerId!!, listenerAudio)
        } else {
            audioCall = callService.takeAudioCall(intent, listenerAudio)

            val peerPhone = audioCall.getPeerPhone()

            database.getUserRoomPeer(peerPhone) {
                liveRoom.value = RoomPeer(
                        avatarUrl = it.avatarUrl,
                        id = it.roomId,
                        name = it.name,
                        peerId = it.peerId
                )
            }
        }
    }

    fun addNewCallMessage(callType:Int, callTime:Int, isMissed:Boolean, isCancel:Boolean=false) {
        val contents = ArrayList<String>().apply {
            add("$callType.$callTime.$isMissed.$isCancel")
        }
        if(liveRoom.value!!.memberMap==null){
            this.contents = contents
            doNeedAddMessage = true
        }else{
            messageManager.addNewMessagesToFirestore(liveRoom.value!!, contents, Message.TYPE_CALL)
        }
    }
}