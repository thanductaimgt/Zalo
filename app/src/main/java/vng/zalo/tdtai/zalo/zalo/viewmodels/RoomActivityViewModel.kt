package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.Message
import vng.zalo.tdtai.zalo.zalo.models.Room
import vng.zalo.tdtai.zalo.zalo.networks.Database
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import java.util.*
import kotlin.collections.HashMap

class RoomActivityViewModel(intent: Intent) : ViewModel() {
    private var lastMessagesQuerySnapshot: QuerySnapshot? = null

    private val room: Room = Room(
            avatar = intent.getStringExtra(Constants.ROOM_AVATAR),
            id = intent.getStringExtra(Constants.ROOM_ID),
            name = intent.getStringExtra(Constants.ROOM_NAME)
    )

    val liveMessages: MutableLiveData<List<Message>> = MutableLiveData(ArrayList())

    init {
        //get current room info
        Database.getRoomInfo(room.id!!) {
            room.createdTime = it.createdTime
            room.memberMap = it.memberMap

            // update livedata so that messages' sender avatar are displayed
            updateLiveMessagesValue(lastMessagesQuerySnapshot)
        }

        //observe messages change
        Database.addRoomMessagesListener(
                roomId = room.id!!,
                fieldToOrder = "createdTime"
        ) { querySnapshot ->
            updateLiveMessagesValue(querySnapshot)
            lastMessagesQuerySnapshot = querySnapshot
        }

        //observe to set unseenMsgNum = 0 when new message comes and user is in RoomActivity
        val curUserPhone = ZaloApplication.currentUser!!.phone!!
        Database.addUserRoomChangeListener(
                userPhone = curUserPhone,
                roomId = room.id!!
        ) { documentSnapshot ->
            if (documentSnapshot.getLong("unseenMsgNum")!! > 0 && curUserPhone == ZaloApplication.currentUser!!.phone) {
                Database.updateUserRoom(
                        userPhone = curUserPhone,
                        roomId = room.id!!,
                        fieldsAndValues = HashMap<String, Any>().apply {
                            put("unseenMsgNum", 0)
                        }
                )
            }
        }
    }

    private fun updateLiveMessagesValue(docs: QuerySnapshot?) {
        //set livedata value
        if (docs != null) {
            val messages = ArrayList<Message>()
            for (doc in docs) {
                messages.add(
                        doc.toObject(Message::class.java).apply {
                            senderAvatar = room.memberMap?.get(senderPhone)?.avatar
                        }
                )
            }
            liveMessages.value = messages
        }
    }

    fun addNewMessageToFirestore(messageContent: String) {
        val message = Message(
                content = messageContent,
                createdTime = Timestamp.now(),
                senderPhone = ZaloApplication.currentUser!!.phone,
                senderAvatar = ZaloApplication.currentUser!!.avatar,
                type = Constants.MESSAGE_TYPE_TEXT
        )

        Database.addNewMessageAndUpdateUsersRoom(
                curRoom = room,
                newMessage = message
        )
    }
}