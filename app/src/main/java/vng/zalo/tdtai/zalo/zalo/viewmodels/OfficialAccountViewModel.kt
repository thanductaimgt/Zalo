package vng.zalo.tdtai.zalo.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import java.util.*

class OfficialAccountViewModel : ViewModel() {
    val liveOfficialAccounts: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())
}