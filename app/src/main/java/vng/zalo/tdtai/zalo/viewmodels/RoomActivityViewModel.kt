package vng.zalo.tdtai.zalo.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.MessageCreator
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.models.message.TypingMessage
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants


class RoomActivityViewModel(intent: Intent) : ViewModel() {
    private var listenerRegistrations = ArrayList<ListenerRegistration>()
    private var lastMessages: List<Message> = ArrayList()
    private val messageCreator = MessageCreator()

    val room: Room = Room(
            avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
            id = intent.getStringExtra(Constants.ROOM_ID),
            name = intent.getStringExtra(Constants.ROOM_NAME)
    )

    val liveMessages: MutableLiveData<List<Message>> = MutableLiveData(ArrayList())
    val liveTypingPhones: MutableLiveData<List<String>> = MutableLiveData(ArrayList())

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
                        userPhone = ZaloApplication.curUser!!.phone!!,
                        roomId = room.id!!
                ) { documentSnapshot ->
                    if (documentSnapshot.getLong(RoomItem.FIELD_UNSEEN_MSG_NUM)!! > 0) {
                        Database.updateUserRoom(
                                userPhone = ZaloApplication.curUser!!.phone!!,
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
            liveTypingPhones.value = (documentSnapshot.get(Room.FIELD_TYPING_MEMBERS_PHONE)
                    ?: ArrayList<String>()) as List<String>
        }
    }

    private fun updateMessages(messages: List<Message> = lastMessages) {
        if (messages.isNotEmpty() && room.memberMap != null) {
            messages.forEach {
                it.senderAvatarUrl = room.memberMap!![it.senderPhone]!!.avatarUrl
            }
            liveMessages.value = messages
        }
        lastMessages = messages
    }

    fun addNewMessagesToFirestore(context: Context, contents: List<String>, type: Int) {
        messageCreator.addNewMessagesToFirestore(context, room, contents, type)
    }

    fun addCurUserToCurRoomTypingMembers() {
        if (!liveTypingPhones.value!!.contains(ZaloApplication.curUser!!.phone)) {
            Database.addCurrentUserToRoomTypingMembers(room.id!!)
        }
    }

    fun removeCurUserFromCurRoomTypingMembers() {
        if (liveTypingPhones.value!!.contains(ZaloApplication.curUser!!.phone)) {
            Database.removeCurrentUserToRoomTypingMembers(room.id!!)
        }
    }

    fun getTypingMessages(phones: List<String>): ArrayList<Message> {
        val typingMessages = ArrayList<Message>()
        phones.filter { it != ZaloApplication.curUser!!.phone }.reversed().forEach {
            typingMessages.add(TypingMessage(
                    id = it,
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