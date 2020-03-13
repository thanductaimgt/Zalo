package vng.zalo.tdtai.zalo.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.model.RoomMember
import vng.zalo.tdtai.zalo.model.room.Room
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.model.room.RoomItemGroup
import vng.zalo.tdtai.zalo.model.room.RoomItemPeer
import vng.zalo.tdtai.zalo.repo.Database
import javax.inject.Inject

class RoomItemsViewModel @Inject constructor(
        private val database: Database,
        sessionManager: SessionManager
) : ViewModel() {
    private var roomIdLastOnlineTimeMap = HashMap<String, Long?>()
    private var roomIdLastTypingMemberMap = HashMap<String, RoomMember?>()
    val liveRoomItemMap = MutableLiveData<HashMap<String, RoomItem>>(HashMap())
    var listenerRegistrations = ArrayList<ListenerRegistration>()
//    var isFirstLoad = true

    private var lastOnlineTimeListenerRegistration: ListenerRegistration? = null
    private var roomsTypingListenerRegistration: ListenerRegistration? = null

    init {
        val userRoomsListener = database.addUserRoomsListener(
                userPhone = sessionManager.curUser!!.phone!!
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
                    put((it as RoomItemPeer).phone!!, it.roomId!!)
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
            if (roomItem.lastTypingMember?.phone != lastTypingMember?.phone) {
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