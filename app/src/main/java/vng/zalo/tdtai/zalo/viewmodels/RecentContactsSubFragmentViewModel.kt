package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database
import java.util.*

class RecentContactsSubFragmentViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    init {
        Database.getUserRooms(
                userPhone = ZaloApplication.curUser!!.phone!!,
                roomType = RoomItem.TYPE_PEER,
                fieldToOrder = RoomItem.FIELD_LAST_MSG_TIME,
                orderDirection = Query.Direction.DESCENDING
        ) { liveRoomItems.value = it }
    }
}