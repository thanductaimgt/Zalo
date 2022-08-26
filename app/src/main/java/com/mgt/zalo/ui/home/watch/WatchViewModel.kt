package com.mgt.zalo.ui.home.watch

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.data_model.User
import com.mgt.zalo.data_model.post.Watch
import com.mgt.zalo.util.TAG
import javax.inject.Inject

class WatchViewModel @Inject constructor() : BaseViewModel() {
    val liveWatches = MutableLiveData(ArrayList<Watch>())

    private val interestedUsersId = arrayListOf("0123456789", "0987654321", "0111111111")

    var lastWatches: ArrayList<Watch>? = null

    var idUserMap: HashMap<String, User>? = null

    init {
        database.getUsers(interestedUsersId) { users ->
            val map = hashMapOf<String, User>()
            users.forEach { user ->
                map[user.id!!] = user
            }
            idUserMap = map
            updateLiveWatches()
        }
        loadMoreWatches()
    }

    fun refreshRecentWatches(callback: (() -> Unit)? = null) {
        upperBoundTime = null
        liveWatches.value!!.clear()
        loadMoreWatches(callback)
    }

    private fun updateLiveWatches() {
        if (idUserMap != null && lastWatches != null && lastWatches!!.isNotEmpty()) {
            var user: User
            lastWatches!!.forEach { diary ->
                user = idUserMap!![diary.ownerId!!]!!
                diary.ownerName = user.name
                diary.ownerAvatarUrl = user.avatarUrl
            }
            liveWatches.value = liveWatches.value!!.apply { addAll(lastWatches!!) }
        }
    }

    private var upperBoundTime:Long? = null
    private var isLoading = false

    fun loadMoreWatches(callback: (() -> Unit)? = null){
        isLoading = true

        database.getUsersRecentWatches(
                usersId = interestedUsersId,
                upperCreatedTimeLimit = upperBoundTime,
                numberLimit = 10
        ) { watches ->
            lastWatches = watches
            upperBoundTime = watches.minByOrNull { it.createdTime!! }?.createdTime
            Log.d(TAG, "more upper: $upperBoundTime")
            updateLiveWatches()
            isLoading = false
            callback?.invoke()
        }

        Log.d(TAG, "load more diaries")
    }

    fun shouldLoadMoreWatches(): Boolean {
        return upperBoundTime != null && !isLoading
    }
}