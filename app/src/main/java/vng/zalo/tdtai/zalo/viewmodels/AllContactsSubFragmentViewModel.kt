package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database
import java.util.*

class AllContactsSubFragmentViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    init {
        Database.getUserRooms(
                userPhone = ZaloApplication.curUser!!.phone!!,
                roomType = RoomItem.TYPE_PEER,
                fieldToOrder = RoomItem.FIELD_NAME
        ) { liveRoomItems.value = it }
    }
}