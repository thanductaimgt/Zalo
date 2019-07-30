package vng.zalo.tdtai.zalo.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.models.Sticker
import vng.zalo.tdtai.zalo.zalo.models.StickerSet
import vng.zalo.tdtai.zalo.zalo.networks.Database
import java.util.*

class StickerSetViewModel(bucketName: String) : ViewModel() {

    val liveStickerSet: MutableLiveData<StickerSet> = MutableLiveData()

    init {
        Database.getStickerSet(bucketName){ liveStickerSet.value = it }
    }
}