package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.networks.Database

class ChatFragmentViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())
    var listenerRegistrations = ArrayList<ListenerRegistration>()

    init {
        viewModelScope.launch {

        }
        val userRoomListener = Database.addUserRoomsListener(
                userPhone = ZaloApplication.currentUser!!.phone!!,
                fieldToOrder = "lastMsgTime",
                orderDirection = Query.Direction.DESCENDING
        ) { liveRoomItems.value = it }
        listenerRegistrations.add(userRoomListener)
    }

    fun removeListeners() {
        listenerRegistrations.forEach { it.remove() }
        Log.d("removeListeners", "finalize")
    }
}