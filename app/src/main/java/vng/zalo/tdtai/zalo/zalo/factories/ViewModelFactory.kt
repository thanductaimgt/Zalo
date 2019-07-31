package vng.zalo.tdtai.zalo.zalo.factories

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vng.zalo.tdtai.zalo.zalo.viewmodels.*

class ViewModelFactory private constructor() : ViewModelProvider.Factory {
    private lateinit var intent: Intent
    private lateinit var bucketName: String

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            RoomActivityViewModel::class.java -> RoomActivityViewModel(intent) as T
            StickerSetViewModel::class.java -> StickerSetViewModel(bucketName) as T
            else -> modelClass.newInstance()
        }
    }

    companion object {
        private var instance: ViewModelFactory? = null

        fun getInstance(intent: Intent? = null, bucket_name: String? = null): ViewModelFactory {
            if (instance == null) {
                synchronized(ViewModelFactory) {
                    if (instance == null) {
                        instance = ViewModelFactory()
                    }
                }
            }

            if (intent != null) {
                instance!!.intent = intent
            } else if (bucket_name != null) {
                instance!!.bucketName = bucket_name
            }
            return instance!!
        }
    }
}