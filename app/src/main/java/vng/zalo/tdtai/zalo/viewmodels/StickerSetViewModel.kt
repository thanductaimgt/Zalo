package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.models.StickerSet
import vng.zalo.tdtai.zalo.storage.FirebaseDatabase

class StickerSetViewModel(bucketName: String) : ViewModel() {

    val liveStickerSet: MutableLiveData<StickerSet> = MutableLiveData()

    init {
        FirebaseDatabase.getStickerSet(bucketName) { liveStickerSet.value = it }
    }
}