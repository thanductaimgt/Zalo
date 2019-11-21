package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database

class UserRoomItemsViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())
    var listenerRegistrations = ArrayList<ListenerRegistration>()

    init {
        val userRoomsListener = Database.addUserRoomsListener(
                userPhone = ZaloApplication.curUser!!.phone!!,
                fieldToOrder = RoomItem.FIELD_LAST_MSG_TIME,
                orderDirection = Query.Direction.DESCENDING
        ) { liveRoomItems.value = it }
        listenerRegistrations.add(userRoomsListener)
    }

    fun removeListeners() {
        listenerRegistrations.forEach { it.remove() }
    }
}