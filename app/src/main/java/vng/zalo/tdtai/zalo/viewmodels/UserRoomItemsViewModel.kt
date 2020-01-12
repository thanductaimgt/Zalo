package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.room.RoomItemGroup
import vng.zalo.tdtai.zalo.models.room.RoomItemPeer
import vng.zalo.tdtai.zalo.networks.Database

class UserRoomItemsViewModel : ViewModel() {
    private var roomIdLastOnlineTimeMap = HashMap<String, Timestamp?>()
    private var roomIdLastTypingPhoneMap = HashMap<String, String?>()
    val liveRoomIdRoomItemMap = MutableLiveData<HashMap<String, RoomItem>>(HashMap())
    var listenerRegistrations = ArrayList<ListenerRegistration>()
    var isFirstLoad = true

    private var lastOnlineTimeListenerRegistration: ListenerRegistration? = null
    private var roomsTypingListenerRegistration: ListenerRegistration? = null

    init {
        val userRoomsListener = Database.addUserRoomsListener(
                userPhone = ZaloApplication.curUser!!.phone!!
        ) { roomItems ->
            roomItems.sortedByDescending { it.lastMsgTime }
            updateNewRoomItems(roomItems)

            //refresh observers
            if (isFirstLoad || liveRoomIdRoomItemMap.value!!.size != roomItems.size) {
                isFirstLoad = false
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

                lastOnlineTimeListenerRegistration = Database.addUsersLastOnlineTimeListener(phoneRoomIdMap) { lastOnlineTimeMap ->
                    this.roomIdLastOnlineTimeMap = lastOnlineTimeMap

                    updateOnlineTime()
                }

                //observe new room items last typing phone
                val roomsId = roomItems.map { it.roomId!! }

                roomsTypingListenerRegistration = Database.addRoomsTypingChangeListener(roomsId) { typingPhoneMap ->
                    this.roomIdLastTypingPhoneMap = typingPhoneMap

                    updateTyping()
                }

                lastOnlineTimeListenerRegistration?.let { listenerRegistrations.add(it) }
                roomsTypingListenerRegistration?.let { listenerRegistrations.add(it) }
            }
        }

        listenerRegistrations.add(userRoomsListener)
    }

    private fun updateOnlineTime() {
        roomIdLastOnlineTimeMap.forEach { entry ->
            val peerPhone = entry.key
            val lastOnlineTime = entry.value

            val roomItem = liveRoomIdRoomItemMap.value!![peerPhone]!! as RoomItemPeer
            if (roomItem.lastOnlineTime != lastOnlineTime) {
                liveRoomIdRoomItemMap.value!![peerPhone] = roomItem.copy(lastOnlineTime = lastOnlineTime)
            }
        }
        liveRoomIdRoomItemMap.value = liveRoomIdRoomItemMap.value
    }

    private fun updateTyping() {
        roomIdLastTypingPhoneMap.forEach { entry ->
            val roomId = entry.key
            val lastTypingPhone = entry.value

            val roomItem = liveRoomIdRoomItemMap.value!![roomId]!!
            if (roomItem.lastTypingPhone != lastTypingPhone) {
                liveRoomIdRoomItemMap.value!![roomId] = if (roomItem.roomType == Room.TYPE_PEER) {
                    (roomItem as RoomItemPeer).copy(lastTypingPhone = lastTypingPhone)
                } else {
                    (roomItem as RoomItemGroup).copy(lastTypingPhone = lastTypingPhone)
                }
            }
        }
        liveRoomIdRoomItemMap.value = liveRoomIdRoomItemMap.value
    }

    private fun updateNewRoomItems(roomItems: List<RoomItem>) {
        val newRoomItems = HashMap<String, RoomItem>()
        roomItems.forEach { roomItem ->
            newRoomItems[roomItem.roomId!!] = roomItem.apply {
                lastTypingPhone = roomIdLastTypingPhoneMap[roomId]
                if (roomType == Room.TYPE_PEER) {
                    (roomItem as RoomItemPeer).lastOnlineTime = roomIdLastOnlineTimeMap[roomId]
                }
            }
        }
        liveRoomIdRoomItemMap.value = newRoomItems
    }

    fun removeListeners() {
        listenerRegistrations.forEach { it.remove() }
    }
}