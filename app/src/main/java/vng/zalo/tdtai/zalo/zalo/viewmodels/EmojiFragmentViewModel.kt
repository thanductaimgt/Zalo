package vng.zalo.tdtai.zalo.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.StickerSetItem
import vng.zalo.tdtai.zalo.zalo.networks.Database

class EmojiFragmentViewModel : ViewModel() {
    val liveStickerSetItems: MutableLiveData<List<StickerSetItem>> = MutableLiveData(ArrayList())

    init {
        Database.getUserStickerSetItems(ZaloApplication.currentUser!!.phone!!) {
            liveStickerSetItems.value = it
        }
    }
}