package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.model.StickerSet
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.repo.FirebaseDatabase
import javax.inject.Inject

class StickerSetViewModel @Inject constructor(
        bucketName: String,
        database: Database
) : ViewModel() {

    val liveStickerSet: MutableLiveData<StickerSet> = MutableLiveData()

    init {
        database.getStickerSet(bucketName) { liveStickerSet.value = it }
    }
}