package vng.zalo.tdtai.zalo.ui.call

import android.content.Context
import android.content.Intent
import android.net.sip.SipSession
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.managers.CallService
import vng.zalo.tdtai.zalo.managers.MessageManager
import vng.zalo.tdtai.zalo.model.message.Message
import vng.zalo.tdtai.zalo.model.room.RoomPeer
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Inject


class CallViewModel @Inject constructor(
        private val messageManager: MessageManager,
        intent: Intent,
        listenerAudio:CallService.AudioCallListener,
        database: Database,
        callService: CallService
) : ViewModel() {
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
                    phone = intent.getStringExtra(Constants.ROOM_PHONE)
            )

            //get current room info
            database.getRoomInfo(liveRoom.value!!.id!!) {
                liveRoom.value!!.createdTime = it.createdTime
                liveRoom.value!!.memberMap = it.memberMap

                if(doNeedAddMessage){
                    messageManager.addNewMessagesToFirestore(liveRoom.value!!, contents, Message.TYPE_CALL)
                }
            }

            audioCall = callService.makeAudioCall(liveRoom.value!!.phone!!, listenerAudio)
        } else {
            audioCall = callService.takeAudioCall(intent, listenerAudio)

            val peerPhone = audioCall.getPeerPhone()

            database.getUserRoomPeer(peerPhone) {
                liveRoom.value = RoomPeer(
                        avatarUrl = it.avatarUrl,
                        id = it.roomId,
                        name = it.name,
                        phone = it.phone
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