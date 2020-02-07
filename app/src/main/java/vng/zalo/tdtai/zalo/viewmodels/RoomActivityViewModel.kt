package vng.zalo.tdtai.zalo.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import io.reactivex.Observer
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.MessageManager
import vng.zalo.tdtai.zalo.models.RoomMember
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.models.message.SeenMessage
import vng.zalo.tdtai.zalo.models.message.TypingMessage
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomGroup
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.room.RoomPeer
import vng.zalo.tdtai.zalo.storage.FirebaseDatabase
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils


class RoomActivityViewModel(intent: Intent) : ViewModel() {
    private var listenerRegistrations = ArrayList<ListenerRegistration>()
    private var messagesListenerRegistration: ListenerRegistration? = null

    val livePeerLastOnlineTime = MutableLiveData<Timestamp>(Constants.TIMESTAMP_EPOCH)

    val room: Room

    val liveMessageMap: MutableLiveData<HashMap<String, Message>> = MutableLiveData(HashMap())
    val liveTypingMembers: MutableLiveData<ArrayList<RoomMember>> = MutableLiveData(ArrayList())
    val liveSeenPhones: MutableLiveData<List<String>> = MutableLiveData(ArrayList())
    private var isSeenListLoaded: Boolean = false

    private var lastBoundMessage: Message? = null
    private var isFirstLoad: Boolean = true
    val liveIsLoading = MutableLiveData<Boolean>(false)

    init {
        val phone = intent.getStringExtra(Constants.ROOM_PHONE)

        room = if (phone != null) {
            RoomPeer(
                    avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
                    id = intent.getStringExtra(Constants.ROOM_ID),
                    name = intent.getStringExtra(Constants.ROOM_NAME),
                    phone = phone
            )
        } else {
            RoomGroup(
                    avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
                    id = intent.getStringExtra(Constants.ROOM_ID),
                    name = intent.getStringExtra(Constants.ROOM_NAME)
            )
        }

        //get messages
        loadMoreMessages()

        //get current room info
        FirebaseDatabase.getRoomInfo(room.id!!) {
            room.createdTime = it.createdTime
            room.memberMap = it.memberMap

            if (isSeenListLoaded) {
                liveSeenPhones.value = liveSeenPhones.value
            }

            // update livedata so that messages' sender avatarUrl are displayed
            refreshMessages()
        }

        //observe to set unseenMsgNum = 0 when new message comes and user is in RoomActivity
        listenerRegistrations.add(
                FirebaseDatabase.addUserRoomChangeListener(
                        userPhone = ZaloApplication.curUser!!.phone!!,
                        roomId = room.id!!
                ) { documentSnapshot ->
                    if (documentSnapshot.getLong(RoomItem.FIELD_UNSEEN_MSG_NUM)!! > 0) {
                        FirebaseDatabase.updateUserRoom(
                                userPhone = ZaloApplication.curUser!!.phone!!,
                                roomId = room.id!!,
                                fieldsAndValues = mapOf(
                                        RoomItem.FIELD_UNSEEN_MSG_NUM to 0
                                )
                        )

                        FirebaseDatabase.addCurUserToRoomSeenMembers(roomId = room.id!!)
                    }
                }
        )

        // observe typing events
        @Suppress("UNCHECKED_CAST")
        listenerRegistrations.add(FirebaseDatabase.addRoomInfoChangeListener(room.id!!) { documentSnapshot ->
            //typing members
            val newTypingMembers = ((documentSnapshot.get(Room.FIELD_TYPING_MEMBERS)
                    ?: ArrayList<Any>()) as ArrayList<Any>).map {
                RoomMember.fromObject(it)
            }

            val diffTypingMembers = Utils.getTwoListDiffElement(liveTypingMembers.value!!, newTypingMembers)

            if (diffTypingMembers.size == 1 && diffTypingMembers[0].phone == ZaloApplication.curUser!!.phone) {
                liveTypingMembers.value!!.apply { clear(); addAll(newTypingMembers) }
            } else {
                liveTypingMembers.value = ArrayList(newTypingMembers)
            }

            //seen members
            val newSeenPhones = ((documentSnapshot.get(Room.FIELD_SEEN_MEMBERS_PHONE)
                    ?: ArrayList<String>()) as List<String>).toMutableList().filter { it != ZaloApplication.curUser!!.phone }.asReversed()

            liveSeenPhones.value = newSeenPhones

            if (!isSeenListLoaded) {
                isSeenListLoaded = true
            }
        })

        //observe room online state
        if (room is RoomPeer) {
            listenerRegistrations.add(FirebaseDatabase.addUserLastOnlineTimeListener(room.phone!!) { lastOnlineTime ->
                if (lastOnlineTime != Constants.TIMESTAMP_EPOCH) {
                    livePeerLastOnlineTime.value = lastOnlineTime
                }
            })
        }
    }

    private fun reObserveMessages(boundMessage: Message) {
        messagesListenerRegistration?.let {
            it.remove()
            listenerRegistrations.remove(it)
        }

        messagesListenerRegistration = FirebaseDatabase.addRoomMessagesListener(
                roomId = room.id!!,
                lowerCreatedTimeLimit = boundMessage.createdTime!!
        ) {
            submitMessages(it)
        }
        listenerRegistrations.add(messagesListenerRegistration!!)
    }

    fun loadMoreMessages(boundMessage: Message? = null) {
        isFirstLoad = false
        liveIsLoading.value = true

        FirebaseDatabase.getMessages(room.id!!, boundMessage?.createdTime, MESSAGE_LOAD_NUM) {
            it.lastOrNull()?.let { newBoundMessage ->
                reObserveMessages(newBoundMessage)
                lastBoundMessage = newBoundMessage
            }
            liveIsLoading.value = false
        }

        Log.d(TAG, "load messages")
    }

    fun shouldLoadMoreMessages(): Boolean {
        return !isFirstLoad && lastBoundMessage != null && !liveIsLoading.value!!
    }

    private fun submitMessages(messages: List<Message>) {
        val messageMap = HashMap<String, Message>()

        //todo
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
            !messageMap.containsKey(it.id!!) && !it.isSent
        }.forEach {
                    messageMap[it.id!!] = it
                }

        liveMessageMap.value = messageMap
    }

    fun addOrUpdateMessage(message: Message) {
        liveMessageMap.value!![message.id!!] = message
        refreshMessages()
    }

    private fun refreshMessages() {
        submitMessages(liveMessageMap.value!!.values.toList())
    }

    fun addNewMessagesToFirestore(context: Context, contents: List<String>, type: Int, observer: Observer<Message>) {
        MessageManager.addNewMessagesToFirestore(context, room, contents, type, observer)
    }

    fun addCurUserToCurRoomTypingMembers() {
        if (liveTypingMembers.value!!.all { it.phone != ZaloApplication.curUser!!.phone }) {
            FirebaseDatabase.addCurUserToRoomTypings(room.id!!)
        }
    }

    fun removeCurUserFromCurRoomTypingMembers() {
        if (liveTypingMembers.value!!.any { it.phone == ZaloApplication.curUser!!.phone }) {
            FirebaseDatabase.removeCurUserFromRoomTypings(room.id!!)
        }
    }

    fun getCurRealMessages(): List<Message> {
        return liveMessageMap.value!!.values.sortedByDescending { it.createdTime }
    }

    fun getCurTypingMessages(): List<Message> {
        val typingMessages = ArrayList<Message>()
        liveTypingMembers.value!!.map { it.phone }.filter { it != ZaloApplication.curUser!!.phone }.reversed().forEach {
            typingMessages.add(TypingMessage(
                    id = it,
                    createdTime = Timestamp.now(),
                    senderPhone = it,
                    senderAvatarUrl = room.memberMap?.get(it)?.avatarUrl
            ))
        }
        return typingMessages
    }

    fun getCurSeenMessage(): SeenMessage {
        return SeenMessage(
                id = SeenMessage.ID,
                seenMembers = room.memberMap?.let { memberMap -> liveSeenPhones.value!!.map { memberMap.getValue(it) } }
                        ?: ArrayList()
        )
    }

    override fun onCleared() {
        removeAllListeners()
    }

    fun removeAllListeners() {
        listenerRegistrations.forEach { it.remove() }
    }

    fun getNameFromPhone(phone: String): String {
        return room.memberMap?.get(phone)?.name ?: phone
    }

    companion object {
        const val MESSAGE_LOAD_NUM = 30L
    }
}