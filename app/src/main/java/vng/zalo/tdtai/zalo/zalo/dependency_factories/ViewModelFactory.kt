package vng.zalo.tdtai.zalo.zalo.dependency_factories

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vng.zalo.tdtai.zalo.zalo.viewmodels.*

class ViewModelFactory() : ViewModelProvider.Factory {
    companion object{
        val TAG = ViewModelFactory::class.java.simpleName
    }

    private lateinit var intent:Intent

    constructor(intent: Intent) : this() {
        this.intent = intent
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass){
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
}