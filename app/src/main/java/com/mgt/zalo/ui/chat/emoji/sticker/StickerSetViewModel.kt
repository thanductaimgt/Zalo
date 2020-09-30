package com.mgt.zalo.ui.chat.emoji.sticker

import androidx.lifecycle.MutableLiveData
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.data_model.StickerSet
import javax.inject.Inject

class StickerSetViewModel @Inject constructor(bucketName: String) : BaseViewModel() {

    val liveStickerSet: MutableLiveData<StickerSet> = MutableLiveData()

    init {
        database.getStickerSet(bucketName) { liveStickerSet.value = it }
    }
}