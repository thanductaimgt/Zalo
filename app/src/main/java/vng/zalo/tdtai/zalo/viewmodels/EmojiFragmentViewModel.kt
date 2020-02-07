package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.StickerSetItem
import vng.zalo.tdtai.zalo.storage.FirebaseDatabase

class EmojiFragmentViewModel : ViewModel() {
    val liveStickerSetItems: MutableLiveData<List<StickerSetItem>> = MutableLiveData(ArrayList())

    init {
        FirebaseDatabase.getUserStickerSetItems(ZaloApplication.curUser!!.phone!!) {
            liveStickerSetItems.value = it
        }
    }
}