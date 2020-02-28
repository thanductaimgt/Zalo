package vng.zalo.tdtai.zalo.ui.chat.emoji

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.model.StickerSetItem
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.repo.FirebaseDatabase
import javax.inject.Inject

class EmojiViewModel @Inject constructor(
        database: Database,
        sessionManager: SessionManager
) : ViewModel() {
    val liveStickerSetItems: MutableLiveData<List<StickerSetItem>> = MutableLiveData(ArrayList())

    init {
        database.getUserStickerSetItems(sessionManager.curUser!!.phone!!) {
            liveStickerSetItems.value = it
        }
    }
}