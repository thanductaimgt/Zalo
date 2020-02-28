package vng.zalo.tdtai.zalo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

//class ViewModelFactory private constructor() : ViewModelProvider.Factory {
//    private lateinit var intent: Intent
//    private lateinit var bucketName: String
//    private lateinit var sipAudioCallListener: SipAudioCall.Listener
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return when (modelClass) {
//            ChatViewModel::class.java -> ChatViewModel(intent) as T
//            ShareActivityViewModel::class.java -> ShareActivityViewModel(intent) as T
//            CallActivityViewModel::class.java -> CallActivityViewModel(intent, sipAudioCallListener) as T
//            StickerSetViewModel::class.java -> StickerSetViewModel(bucketName) as T
//            else -> modelClass.newInstance()
//        }
//    }
//
//    companion object {
//        private var instance: ViewModelFactory? = null
//
//        fun getInstance(intent: Intent? = null, bucketName: String? = null, sipAudioCallListener: SipAudioCall.Listener?=null): ViewModelFactory {
//            if (instance == null) {
//                synchronized(ViewModelFactory) {
//                    if (instance == null) {
//                        instance = ViewModelFactory()
//                    }
//                }
//            }
//
//            intent?.let { instance!!.intent = it }
//            bucketName?.let { instance!!.bucketName = it }
//            sipAudioCallListener?.let { instance!!.sipAudioCallListener = it }
//            return instance!!
//        }
//    }
//}

@Singleton
class ViewModelFactory @Inject constructor(
        private val creators: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<out ViewModel>? = creators[modelClass]
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }
        if (creator == null) {
            throw IllegalArgumentException("Unknown model class: $modelClass")
        }
        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}