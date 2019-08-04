package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.models.StickerSet
import vng.zalo.tdtai.zalo.networks.Database

class StickerSetViewModel(bucketName: String) : ViewModel() {

    val liveStickerSet: MutableLiveData<StickerSet> = MutableLiveData()

    init {
        Database.getStickerSet(bucketName) { liveStickerSet.value = it }
    }
}