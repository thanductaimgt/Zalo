package com.mgt.zalo.ui.chat.emoji

import androidx.lifecycle.MutableLiveData
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.data_model.StickerSetItem
import javax.inject.Inject

class EmojiViewModel @Inject constructor() : BaseViewModel() {
    val liveStickerSetItems: MutableLiveData<List<StickerSetItem>> = MutableLiveData(ArrayList())

    init {
        database.getUserStickerSetItems(sessionManager.curUser!!.id!!) {
            liveStickerSetItems.value = it
        }
    }
}