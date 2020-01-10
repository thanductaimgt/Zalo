package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database

class UserRoomItemsViewModel : ViewModel() {
    var lastOnlineTimeMap = HashMap<String, Timestamp?>()
    val liveRoomItems = MutableLiveData<List<RoomItem>>(ArrayList())
    var listenerRegistrations = ArrayList<ListenerRegistration>()

    private var lastOnlineTimeListenerRegistration: ListenerRegistration? = null

    init {
        val userRoomsListener = Database.addUserRoomsListener(
                userPhone = ZaloApplication.curUser!!.phone!!,
                fieldToOrder = RoomItem.FIELD_LAST_MSG_TIME,
                orderDirection = Query.Direction.DESCENDING
        ) { roomItems ->
            if (liveRoomItems.value!!.size != roomItems.size) {
                lastOnlineTimeListenerRegistration?.let { listenerRegistration ->
                    listenerRegistration.remove()
                    listenerRegistrations.remove(listenerRegistration)
                }

                val usersName = roomItems.filter { it.roomType == Room.TYPE_PEER }.map { it.name!! }
                //each time user rooms change, get online states of all contacts, then notify observers
                lastOnlineTimeListenerRegistration = Database.addUsersLastOnlineTimeListener(usersName) { lastOnlineTimeMap ->
                    this.lastOnlineTimeMap = lastOnlineTimeMap

                    updateLiveRoomItems()
                }

                listenerRegistrations.add(lastOnlineTimeListenerRegistration!!)
            }

            updateLiveRoomItems(roomItems)
        }

        listenerRegistrations.add(userRoomsListener)
    }

    private fun updateLiveRoomItems(roomItems: List<RoomItem> = liveRoomItems.value!!) {
        liveRoomItems.value = roomItems.map { roomItem ->
            //group user rooms don't contain field lastOnlineTime, so filter
            if (lastOnlineTimeMap.keys.contains(roomItem.name)) {
                roomItem.copy(
                        lastOnlineTime = lastOnlineTimeMap[roomItem.name]
                )
            } else {
                roomItem
            }
        }
    }

    fun removeListeners() {
        listenerRegistrations.forEach { it.remove() }
    }
}