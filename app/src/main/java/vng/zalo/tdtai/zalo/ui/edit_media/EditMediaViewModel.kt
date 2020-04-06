package vng.zalo.tdtai.zalo.ui.edit_media

import android.graphics.Bitmap
import vng.zalo.tdtai.zalo.base.BaseViewModel
import javax.inject.Inject

class EditMediaViewModel @Inject constructor() : BaseViewModel() {
    fun saveImage(bitmap: Bitmap, callback: (uri: String) -> Unit) {
        backgroundWorkManager.single({
            resourceManager.saveBitmap(bitmap)
        }, compositeDisposable, {
            callback(it)
        })
    }
}