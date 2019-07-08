package vng.zalo.tdtai.zalo.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.networks.Database

class ChatFragmentViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    init {
        Database.addUserRoomsChangeListener(
                userPhone = ZaloApplication.currentUser!!.phone!!,
                fieldToOrder = "lastMsgTime",
                orderDirection = Query.Direction.DESCENDING
        ) { querySnapshot ->
            val roomItems = ArrayList<RoomItem>()
            for (doc in querySnapshot) {
                roomItems.add(
                        doc.toObject(RoomItem::class.java).apply {
                            roomId = doc.id
                        }
                )
            }
            liveRoomItems.value = roomItems
        }
    }

    companion object {
        private val TAG = ChatFragmentViewModel::class.java.simpleName
    }
}