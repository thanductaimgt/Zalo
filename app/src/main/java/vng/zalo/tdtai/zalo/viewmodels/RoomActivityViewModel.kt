package vng.zalo.tdtai.zalo.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import io.reactivex.Observer
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.MessageManager
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.models.message.ResourceMessage
import vng.zalo.tdtai.zalo.models.message.TypingMessage
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomGroup
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.room.RoomPeer
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils


class RoomActivityViewModel(intent: Intent) : ViewModel() {
    private var listenerRegistrations = ArrayList<ListenerRegistration>()

    val livePeerLastOnlineTime = MutableLiveData<Timestamp>(Constants.TIMESTAMP_EPOCH)

    val room: Room

    val liveMessageMap: MutableLiveData<HashMap<String, Message>> = MutableLiveData(HashMap())
    val liveTypingPhones: MutableLiveData<List<String>> = MutableLiveData(ArrayList())

    init {
        val roomType = intent.getIntExtra(Constants.ROOM_TYPE, Room.TYPE_PEER)

        room = if (roomType == Room.TYPE_PEER) {
            RoomPeer(
                    avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
                    id = intent.getStringExtra(Constants.ROOM_ID),
                    name = intent.getStringExtra(Constants.ROOM_NAME),
                    phone = intent.getStringExtra(Constants.ROOM_PHONE)
            )
        } else {
            RoomGroup(
                    avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
                    id = intent.getStringExtra(Constants.ROOM_ID),
                    name = intent.getStringExtra(Constants.ROOM_NAME)
            )
        }

        //observe messages change
        listenerRegistrations.add(
                Database.addRoomMessagesListener(
                        roomId = room.id!!
                ) {
                    submitMessages(it)
                }
        )

        //get current room info
        Database.getRoomInfo(room.id!!) {
            room.createdTime = it.createdTime
            room.memberMap = it.memberMap

            // update livedata so that messages' sender avatarUrl are displayed
            refreshMessages()
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
        listenerRegistrations.add(Database.addRoomInfoChangeListener(room.id!!) { documentSnapshot ->
            @Suppress("UNCHECKED_CAST")
            liveTypingPhones.value = (documentSnapshot.get(Room.FIELD_TYPING_MEMBERS_PHONE)
                    ?: ArrayList<String>()) as List<String>
        })

        //observe room online state
        if (room.type == Room.TYPE_PEER) {
            listenerRegistrations.add(Database.addUserLastOnlineTimeListener(room.name!!) { lastOnlineTime ->
                if (lastOnlineTime != Constants.TIMESTAMP_EPOCH) {
                    livePeerLastOnlineTime.value = lastOnlineTime
                }
            })
        }
    }

    fun submitMessages(messages: List<Message>) {
        val messageMap = HashMap<String, Message>()

        messages.forEach {
            val senderAvatarUrl = room.memberMap?.get(it.senderPhone)?.avatarUrl
            val newMessage = if (it.senderAvatarUrl != senderAvatarUrl) {
                it.clone().apply { this.senderAvatarUrl = senderAvatarUrl }
            } else {
                it
            }

            messageMap[it.id!!] = newMessage
        }

        liveMessageMap.value!!.values.filter {
            it is ResourceMessage
                    && !messageMap.containsKey(it.id!!)
                    && !Utils.isNetworkUri(it.url)
        }.forEach {
            messageMap[it.id!!] = it
        }

        liveMessageMap.value = messageMap
    }

    fun addOrUpdateMessage(message: Message) {
        liveMessageMap.value!![message.id!!] = message
        refreshMessages()
    }

    fun refreshMessages() {
        submitMessages(liveMessageMap.value!!.values.toList())
    }

    fun addNewMessagesToFirestore(context: Context, contents: List<String>, type: Int, observer: Observer<Message>) {
        MessageManager.addNewMessagesToFirestore(context, room, contents, type, observer)
    }

    fun addCurUserToCurRoomTypingMembers() {
        if (!liveTypingPhones.value!!.contains(ZaloApplication.curUser!!.phone)) {
            Database.addCurUserToRoomTypings(room.id!!)
        }
    }

    fun removeCurUserFromCurRoomTypingMembers() {
        if (liveTypingPhones.value!!.contains(ZaloApplication.curUser!!.phone)) {
            Database.removeCurUserFromRoomTypings(room.id!!)
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
        removeAllListeners()
    }

    fun removeAllListeners() {
        listenerRegistrations.forEach { it.remove() }
    }

    fun getNameFromPhone(phone:String):String{
        return room.memberMap?.get(phone)?.name ?:phone
    }
}