package vng.zalo.tdtai.zalo.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.StickerSet
import vng.zalo.tdtai.zalo.zalo.networks.Database

class EmojiFragmentViewModel : ViewModel() {
    val liveStickerSets: MutableLiveData<List<StickerSet>> = MutableLiveData(ArrayList())

    init {
        Database.getUserStickerSets(ZaloApplication.currentUser!!.phone!!) {
            liveStickerSets.value = it
        }
    }
}