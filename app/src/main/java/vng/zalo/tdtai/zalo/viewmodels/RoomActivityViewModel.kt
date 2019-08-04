package vng.zalo.tdtai.zalo.viewmodels

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Message
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import java.util.*
import kotlin.collections.HashMap

class RoomActivityViewModel(intent: Intent) : ViewModel() {
    private var lastMessages: List<Message>? = null
    private var listenerRegistrations = ArrayList<ListenerRegistration>()

    private val room: Room = Room(
            avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
            id = intent.getStringExtra(Constants.ROOM_ID),
            name = intent.getStringExtra(Constants.ROOM_NAME)
    )

    val liveMessages: MutableLiveData<List<Message>> = MutableLiveData(ArrayList())

    init {
        //get current room info
        Database.getRoomInfo(room.id!!) {
            room.createdTime = it.createdTime
            room.memberMap = it.memberMap

            // update livedata so that messages' sender avatarUrl are displayed
            updateLiveMessagesValue(lastMessages)
        }

        //observe messages change
        val roomMessagesListener = Database.addRoomMessagesListener(
                roomId = room.id!!,
                fieldToOrder = "createdTime",
                orderDirection = Query.Direction.DESCENDING
        ) { messages ->
            updateLiveMessagesValue(messages)
            lastMessages = messages
        }
        listenerRegistrations.add(roomMessagesListener)

        //observe to set unseenMsgNum = 0 when new message comes and user is in RoomActivity
        val userRoomChangeListener = Database.addUserRoomChangeListener(
                userPhone = ZaloApplication.currentUser!!.phone!!,
                roomId = room.id!!
        ) { documentSnapshot ->
            if (documentSnapshot.getLong("unseenMsgNum")!! > 0) {
                Database.updateUserRoom(
                        userPhone = ZaloApplication.currentUser!!.phone!!,
                        roomId = room.id!!,
                        fieldsAndValues = HashMap<String, Any>().apply {
                            put("unseenMsgNum", 0)
                        }
                )
            }
        }
        listenerRegistrations.add(userRoomChangeListener)
    }

    private fun updateLiveMessagesValue(messages: List<Message>?) {
        //set livedata value
        Utils.assertNotNull(messages, TAG, "updateLiveMessagesValue") { messagesNotNull ->
            messagesNotNull.forEach {
                it.senderAvatarUrl = room.memberMap?.get(it.senderPhone)?.avatarUrl
            }
            liveMessages.value = messages
        }
    }

    fun addNewMessageToFirestore(content: String, type: Int) {
        val message = Message(
                content = content,
                createdTime = Timestamp.now(),
                senderPhone = ZaloApplication.currentUser!!.phone,
                senderAvatarUrl = ZaloApplication.currentUser!!.avatarUrl,
                type = type
        )

        Database.addNewMessageAndUpdateUsersRoom(
                curRoom = room,
                newMessage = message
        )
    }

    fun removeListeners() {
        listenerRegistrations.forEach { it.remove() }
    }
}