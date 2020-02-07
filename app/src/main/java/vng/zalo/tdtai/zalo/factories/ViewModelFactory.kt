package vng.zalo.tdtai.zalo.factories

import android.content.Intent
import android.net.sip.SipAudioCall
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vng.zalo.tdtai.zalo.viewmodels.CallActivityViewModel
import vng.zalo.tdtai.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.viewmodels.ShareActivityViewModel
import vng.zalo.tdtai.zalo.viewmodels.StickerSetViewModel

class ViewModelFactory private constructor() : ViewModelProvider.Factory {
    private lateinit var intent: Intent
    private lateinit var bucketName: String
    private lateinit var sipAudioCallListener: SipAudioCall.Listener

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            RoomActivityViewModel::class.java -> RoomActivityViewModel(intent) as T
            ShareActivityViewModel::class.java -> ShareActivityViewModel(intent) as T
            CallActivityViewModel::class.java -> CallActivityViewModel(intent, sipAudioCallListener) as T
            StickerSetViewModel::class.java -> StickerSetViewModel(bucketName) as T
            else -> modelClass.newInstance()
        }
    }

    companion object {
        private var instance: ViewModelFactory? = null

        fun getInstance(intent: Intent? = null, bucketName: String? = null, sipAudioCallListener: SipAudioCall.Listener?=null): ViewModelFactory {
            if (instance == null) {
                synchronized(ViewModelFactory) {
                    if (instance == null) {
                        instance = ViewModelFactory()
                    }
                }
            }

            intent?.let { instance!!.intent = it }
            bucketName?.let { instance!!.bucketName = it }
            sipAudioCallListener?.let { instance!!.sipAudioCallListener = it }
            return instance!!
        }
    }
}