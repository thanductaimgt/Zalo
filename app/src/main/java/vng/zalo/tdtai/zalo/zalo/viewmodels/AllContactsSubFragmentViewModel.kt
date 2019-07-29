package vng.zalo.tdtai.zalo.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.networks.Database
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import java.util.*

class AllContactsSubFragmentViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    init {
        Database.getUserRooms(
                userPhone = ZaloApplication.currentUser!!.phone!!,
                roomType = Constants.ROOM_TYPE_PEER,
                fieldToOrder = "name"
        ) { liveRoomItems.value = it }
    }
}