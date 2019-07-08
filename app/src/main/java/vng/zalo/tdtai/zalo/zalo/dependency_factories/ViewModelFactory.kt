package vng.zalo.tdtai.zalo.zalo.dependency_factories

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vng.zalo.tdtai.zalo.zalo.viewmodels.*

class ViewModelFactory private constructor() : ViewModelProvider.Factory {
    private lateinit var intent: Intent

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ChatFragmentViewModel::class.java -> ChatFragmentViewModel() as T
            ContactSubFragmentViewModel::class.java -> ContactSubFragmentViewModel() as T
            GroupFragmentViewModel::class.java -> GroupFragmentViewModel() as T
            RoomActivityViewModel::class.java -> RoomActivityViewModel(intent) as T
            OfficialAccountViewModel::class.java -> OfficialAccountViewModel() as T
            CreateGroupActivityViewModel::class.java -> CreateGroupActivityViewModel() as T
            else -> {
                RecentContactsSubFragmentViewModel() as T
            }
        }
    }

    companion object {
        private var instance: ViewModelFactory? = null

        fun getInstance(intent: Intent? = null): ViewModelFactory {
            if (instance == null) {
                synchronized(ViewModelFactory) {
                    if (instance == null) {
                        instance = ViewModelFactory()
                    }
                }
            }
            if (intent != null) {
                instance!!.intent = intent
            }
            return instance!!
        }
    }
}