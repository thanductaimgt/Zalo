package vng.zalo.tdtai.zalo.ui.home.chat

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.RoomMember
import vng.zalo.tdtai.zalo.data_model.room.Room
import vng.zalo.tdtai.zalo.data_model.room.RoomItem
import vng.zalo.tdtai.zalo.data_model.room.RoomItemGroup
import vng.zalo.tdtai.zalo.data_model.room.RoomItemPeer
import javax.inject.Inject

class ChatFragmentViewModel @Inject constructor() : BaseViewModel() {
    private var roomIdLastOnlineTimeMap = HashMap<String, Long?>()
    private var roomIdLastTypingMemberMap = HashMap<String, RoomMember?>()
    val liveRoomItemMap = MutableLiveData<HashMap<String, RoomItem>>(HashMap())

    private var lastOnlineTimeListenerRegistration: ListenerRegistration? = null
    private var roomsTypingListenerRegistration: ListenerRegistration? = null

    init {
        val userRoomsListener = database.addUserRoomsListener(
                userId = sessionManager.curUser!!.id!!
        ) { roomItems ->
            @Suppress("NAME_SHADOWING")
            val roomItems = roomItems.sortedByDescending { it.lastMsgTime }
            updateNewRoomItems(roomItems)

            //refresh observers
            lastOnlineTimeListenerRegistration?.let { listenerRegistration ->
                listenerRegistration.remove()
                listenerRegistrations.remove(listenerRegistration)
            }
            roomsTypingListenerRegistration?.let { listenerRegistration ->
                listenerRegistration.remove()
                listenerRegistrations.remove(listenerRegistration)
            }

            //observe new room items last online time
            val phoneRoomIdMap = HashMap<String, String>().apply {
                roomItems.filter { it.roomType == Room.TYPE_PEER }.forEach {
                    put((it as RoomItemPeer).peerId!!, it.roomId!!)
                }
            }

            lastOnlineTimeListenerRegistration = database.addUsersLastOnlineTimeListener(phoneRoomIdMap) { lastOnlineTimeMap ->
                roomIdLastOnlineTimeMap = lastOnlineTimeMap

                updateOnlineTime()
            }

            //observe new room items last typing phone
            val roomsId = roomItems.map { it.roomId!! }

            roomsTypingListenerRegistration = database.addRoomsTypingChangeListener(roomsId) { typingMemberMap ->
                roomIdLastTypingMemberMap = typingMemberMap

                updateTyping()
            }

            lastOnlineTimeListenerRegistration?.let { listenerRegistrations.add(it) }
            roomsTypingListenerRegistration?.let { listenerRegistrations.add(it) }
        }

        listenerRegistrations.add(userRoomsListener)
    }

    private fun updateOnlineTime() {
        roomIdLastOnlineTimeMap.forEach { entry ->
            val peerPhone = entry.key
            val lastOnlineTime = entry.value

            val roomItem = liveRoomItemMap.value!![peerPhone]!! as RoomItemPeer
            if (roomItem.lastOnlineTime != lastOnlineTime) {
                liveRoomItemMap.value!![peerPhone] = roomItem.copy(lastOnlineTime = lastOnlineTime)
            }
        }
        liveRoomItemMap.value = liveRoomItemMap.value
    }

    private fun updateTyping() {
        roomIdLastTypingMemberMap.forEach { entry ->
            val roomId = entry.key
            val lastTypingMember = entry.value

            val roomItem = liveRoomItemMap.value!![roomId]!!
            if (roomItem.lastTypingMember?.userId != lastTypingMember?.userId) {
                liveRoomItemMap.value!![roomId] = if (roomItem.roomType == Room.TYPE_PEER) {
                    (roomItem as RoomItemPeer).copy(lastTypingMember = lastTypingMember)
                } else {
                    (roomItem as RoomItemGroup).copy(lastTypingMember = lastTypingMember)
                }
            }
        }
        liveRoomItemMap.value = liveRoomItemMap.value
    }

    private fun updateNewRoomItems(roomItems: List<RoomItem>) {
        val newRoomItems = HashMap<String, RoomItem>()
        roomItems.forEach { roomItem ->
            newRoomItems[roomItem.roomId!!] = roomItem.apply {
                lastTypingMember = roomIdLastTypingMemberMap[roomId]
                if (roomType == Room.TYPE_PEER) {
                    (roomItem as RoomItemPeer).lastOnlineTime = roomIdLastOnlineTimeMap[roomId]
                }
            }
        }
        liveRoomItemMap.value = newRoomItems
    }

    fun removeListeners() {
        listenerRegistrations.forEach { it.remove() }
    }
}