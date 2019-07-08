package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.networks.Database
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class ChatFragmentViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    init {
        Log.d(Utils.getTag(object {}),"init ChatFragmentViewModel")
        Database.addUserRoomsListener(
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
}