package vng.zalo.tdtai.zalo.ui.home.contacts.official_account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.model.room.RoomItem
import java.util.*
import javax.inject.Inject

class OfficialAccountViewModel @Inject constructor() : ViewModel() {
    val liveOfficialAccounts: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())
}