package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.StickerSet
import javax.inject.Inject

class StickerSetViewModel @Inject constructor(bucketName: String) : BaseViewModel() {

    val liveStickerSet: MutableLiveData<StickerSet> = MutableLiveData()

    init {
        database.getStickerSet(bucketName) { liveStickerSet.value = it }
    }
}