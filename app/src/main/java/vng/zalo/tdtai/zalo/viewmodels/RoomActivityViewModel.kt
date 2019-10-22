package vng.zalo.tdtai.zalo.viewmodels

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Message
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RoomActivityViewModel(intent: Intent) : ViewModel() {
    private var listenerRegistrations = ArrayList<ListenerRegistration>()
    private var lastMessages:List<Message> = ArrayList()

    val room: Room = Room(
            avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
            id = intent.getStringExtra(Constants.ROOM_ID),
            name = intent.getStringExtra(Constants.ROOM_NAME)
    )

    val liveMessages: MutableLiveData<List<Message>> = MutableLiveData(ArrayList())
    val liveTypingMembersPhone: MutableLiveData<List<String>> = MutableLiveData(ArrayList())

    init {
        //observe messages change
        listenerRegistrations.add(
                Database.addRoomMessagesListener(
                        roomId = room.id!!,
                        fieldToOrder = Message.FIELD_CREATED_TIME,
                        orderDirection = Query.Direction.DESCENDING
                ) {
                    updateMessages(it)
                }
        )

        //get current room info
        Database.getRoomInfo(room.id!!) {
            room.createdTime = it.createdTime
            room.memberMap = it.memberMap

            // update livedata so that messages' sender avatarUrl are displayed
            updateMessages()
        }

        //observe to set unseenMsgNum = 0 when new message comes and user is in RoomActivity
        listenerRegistrations.add(
                Database.addUserRoomChangeListener(
                        userPhone = ZaloApplication.currentUser!!.phone!!,
                        roomId = room.id!!
                ) { documentSnapshot ->
                    if (documentSnapshot.getLong(RoomItem.FIELD_UNSEEN_MSG_NUM)!! > 0) {
                        Database.updateUserRoom(
                                userPhone = ZaloApplication.currentUser!!.phone!!,
                                roomId = room.id!!,
                                fieldsAndValues = HashMap<String, Any>().apply {
                                    put(RoomItem.FIELD_UNSEEN_MSG_NUM, 0)
                                }
                        )
                    }
                }
        )

        // observe typing events
        Database.addRoomInfoChangeListener(room.id!!) { documentSnapshot ->
            @Suppress("UNCHECKED_CAST")
            liveTypingMembersPhone.value = (documentSnapshot.get(Room.FIELD_TYPING_MEMBERS_PHONE)
                    ?: ArrayList<String>()) as List<String>
        }
    }

    private fun updateMessages(messages:List<Message> = lastMessages) {
        if (messages.isNotEmpty() && room.memberMap != null) {
            messages.forEach {
                it.senderAvatarUrl = room.memberMap!![it.senderPhone]!!.avatarUrl
            }
            liveMessages.value = messages
        }
        lastMessages = messages
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

    fun addCurUserToCurRoomTypingMembers() {
        if (!liveTypingMembersPhone.value!!.contains(ZaloApplication.currentUser!!.phone)) {
            Database.addCurrentUserToRoomTypingMembers(room.id!!)
        }
    }

    fun removeCurUserFromCurRoomTypingMembers() {
        if (liveTypingMembersPhone.value!!.contains(ZaloApplication.currentUser!!.phone)) {
            Database.removeCurrentUserToRoomTypingMembers(room.id!!)
        }
    }

    fun getTypingMessages(phones: List<String>): ArrayList<Message> {
        val typingMessages = ArrayList<Message>()
        phones.filter { it != ZaloApplication.currentUser!!.phone }.reversed().forEach {
            typingMessages.add(Message(
                    content = "",
                    createdTime = Timestamp.now(),
                    senderPhone = it,
                    senderAvatarUrl = room.memberMap?.get(it)?.avatarUrl,
                    type = Message.TYPE_TYPING
            ))
        }
        return typingMessages
    }

    override fun onCleared() {
        listenerRegistrations.forEach { it.remove() }
    }
}