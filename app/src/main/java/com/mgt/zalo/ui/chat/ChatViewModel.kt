package com.mgt.zalo.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.data_model.RoomMember
import com.mgt.zalo.data_model.message.Message
import com.mgt.zalo.data_model.message.SeenMessage
import com.mgt.zalo.data_model.message.TypingMessage
import com.mgt.zalo.data_model.room.Room
import com.mgt.zalo.data_model.room.RoomGroup
import com.mgt.zalo.data_model.room.RoomItem
import com.mgt.zalo.data_model.room.RoomPeer
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG
import io.reactivex.Observer
import javax.inject.Inject


class ChatViewModel @Inject constructor(intent: Intent) : BaseViewModel() {
//    private var listenerRegistrations = ArrayList<ListenerRegistration>()
    private var messagesListenerRegistration: ListenerRegistration? = null

    val livePeerLastOnlineTime = MutableLiveData(Constants.TIMESTAMP_EPOCH)

    val room: Room

    val liveMessageMap: MutableLiveData<HashMap<String, Message>> = MutableLiveData(HashMap())
    val liveTypingMembers: MutableLiveData<ArrayList<RoomMember>> = MutableLiveData(ArrayList())
    val liveSeenPhones: MutableLiveData<List<String>> = MutableLiveData(ArrayList())
    private var isSeenListLoaded: Boolean = false

    private var lastBoundMessage: Message? = null
    private var isFirstLoad: Boolean = true
    private var isNewMessagesSubmitted: Boolean = false
    private var lastSubmittedTime: Long? = null
    private var lastMessageMap:HashMap<String, Message>?=null

    val liveIsLoading = MutableLiveData<Boolean>(false)

    init {
        val phone = intent.getStringExtra(Constants.ROOM_PHONE)

        room = if (phone != null) {
            RoomPeer(
                    avatarUrl = intent.getStringExtra(Constants.ROOM_AVATAR),
                    id = intent.getStringExtra(Constants.ROOM_ID),
                    name = intent.getStringExtra(Constants.ROOM_NAME),
                    peerId = phone
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
        database.getRoomInfo(room.id!!) {
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
                database.addUserRoomChangeListener(
                        userId = sessionManager.curUser!!.id!!,
                        roomId = room.id!!
                ) { documentSnapshot ->
                    if (documentSnapshot.getLong(RoomItem.FIELD_UNSEEN_MSG_NUM)!! > 0) {
                        database.updateUserRoom(
                                userId = sessionManager.curUser!!.id!!,
                                roomId = room.id!!,
                                fieldsAndValues = mapOf(
                                        RoomItem.FIELD_UNSEEN_MSG_NUM to 0
                                )
                        )

                        database.addCurUserToRoomSeenMembers(roomId = room.id!!)
                    }
                }
        )

        // observe typing events
        @Suppress("UNCHECKED_CAST")
        listenerRegistrations.add(database.addRoomInfoChangeListener(room.id!!) { documentSnapshot ->
            //typing members
            val newTypingMembers = ((documentSnapshot.get(Room.FIELD_TYPING_MEMBERS)
                    ?: ArrayList<Any>()) as ArrayList<Any>).map {
                RoomMember.fromObject(it)
            }

            val diffTypingMembers = utils.getTwoListDiffElement(liveTypingMembers.value!!, newTypingMembers)

            if (diffTypingMembers.size == 1 && diffTypingMembers[0].userId == sessionManager.curUser!!.id) {
                liveTypingMembers.value!!.apply { clear(); addAll(newTypingMembers) }
            } else {
                liveTypingMembers.value = ArrayList(newTypingMembers)
            }

            //seen members
            val newSeenPhones = ((documentSnapshot.get(Room.FIELD_SEEN_MEMBERS_ID)
                    ?: ArrayList<String>()) as List<String>).toMutableList().filter { it != sessionManager.curUser!!.id }.asReversed()

            liveSeenPhones.value = newSeenPhones

            if (!isSeenListLoaded) {
                isSeenListLoaded = true
            }
        })

        //observe room online state
        if (room is RoomPeer) {
            listenerRegistrations.add(database.addUserLastOnlineTimeListener(room.peerId!!) { lastOnlineTime ->
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

        messagesListenerRegistration = database.addRoomMessagesListener(
                roomId = room.id!!,
                lowerCreatedTimeLimit = boundMessage.createdTime!!
        ) {
            if (isNewMessagesSubmitted) {
                isNewMessagesSubmitted = false
            } else {
                submitMessages(it)
            }
        }
        listenerRegistrations.add(messagesListenerRegistration!!)
    }

    fun loadMoreMessages(boundMessage: Message? = null) {
        isFirstLoad = false
        liveIsLoading.value = true

        database.getMessages(room.id!!, boundMessage?.createdTime, MESSAGE_LOAD_NUM) { messages ->
            lastBoundMessage = messages.lastOrNull()?.also { newBoundMessage ->
                addOrUpdateMessages(messages)
                isNewMessagesSubmitted = true

                reObserveMessages(newBoundMessage)
            }

            liveIsLoading.value = false
        }

        Log.d(TAG, "load messages")
    }

    fun shouldLoadMoreMessages(): Boolean {
        return !isFirstLoad && lastBoundMessage != null && !liveIsLoading.value!!
    }

    @SuppressLint("CheckResult")
    private fun submitMessages(messages: List<Message>) {
        backgroundWorkManager.single({
            val messageMap = HashMap<String, Message>()

            //todo
            messages.forEach {
                val senderAvatarUrl = room.memberMap?.get(it.senderId)?.avatarUrl
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

            messageMap
        }, compositeDisposable,
                {messageMap->
            lastMessageMap = messageMap
            updateLiveMessageMap()
        })
    }

    private fun updateLiveMessageMap() {
        val curTime = System.currentTimeMillis()
        if (lastSubmittedTime == null) {
            liveMessageMap.value = lastMessageMap
            lastSubmittedTime = curTime
        } else {
            val gap = curTime - lastSubmittedTime!!
            if (gap > 100) {
                liveMessageMap.value = lastMessageMap
                lastSubmittedTime = curTime
            } else {
                Handler(Looper.getMainLooper()).postDelayed({ updateLiveMessageMap() }, gap)
            }
        }
    }

    fun addOrUpdateMessage(message: Message) {
        liveMessageMap.value!![message.id!!] = message
        refreshMessages()
    }

    private fun addOrUpdateMessages(messages: List<Message>) {
        messages.forEach { message ->
            liveMessageMap.value!![message.id!!] = message
        }
        refreshMessages()
    }

    private fun refreshMessages() {
        submitMessages(liveMessageMap.value!!.values.toList())
    }

    fun addNewMessagesToFirestore(contents: List<String>, type: Int, observer: Observer<Message>) {
        messageManager.addNewMessagesToFirestore(room, contents, type, observer)
    }

    fun addCurUserToCurRoomTypingMembers() {
        if (liveTypingMembers.value!!.all { it.userId != sessionManager.curUser!!.id }) {
            database.addCurUserToRoomTypings(room.id!!)
        }
    }

    fun removeCurUserFromCurRoomTypingMembers() {
        if (liveTypingMembers.value!!.any { it.userId == sessionManager.curUser!!.id }) {
            database.removeCurUserFromRoomTypings(room.id!!)
        }
    }

    fun getCurRealMessages(): List<Message> {
        return liveMessageMap.value!!.values.sortedByDescending { it.createdTime }
    }

    fun getCurTypingMessages(): List<Message> {
        val typingMessages = ArrayList<Message>()
        liveTypingMembers.value!!.map { it.userId }.filter { it != sessionManager.curUser!!.id }.reversed().forEach {
            typingMessages.add(TypingMessage(
                    id = it,
                    createdTime = System.currentTimeMillis(),
                    senderId = it,
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

//    override fun onCleared() {
//        removeAllListeners()
//    }

//    fun removeAllListeners() {
//        listenerRegistrations.forEach { it.remove() }
//    }

    fun getNameFromPhone(phone: String): String {
        return room.memberMap?.get(phone)?.name ?: phone
    }

    companion object {
        const val MESSAGE_LOAD_NUM = 30L
    }
}