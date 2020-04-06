package vng.zalo.tdtai.zalo.ui.chat.emoji

import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.StickerSetItem
import javax.inject.Inject

class EmojiViewModel @Inject constructor() : BaseViewModel() {
    val liveStickerSetItems: MutableLiveData<List<StickerSetItem>> = MutableLiveData(ArrayList())

    init {
        database.getUserStickerSetItems(sessionManager.curUser!!.id!!) {
            liveStickerSetItems.value = it
        }
    }
}