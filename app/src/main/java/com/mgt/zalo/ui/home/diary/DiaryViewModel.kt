package com.mgt.zalo.ui.home.diary

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.data_model.User
import com.mgt.zalo.data_model.post.Diary
import com.mgt.zalo.data_model.story.StoryGroup
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG
import javax.inject.Inject

class DiaryViewModel @Inject constructor() : BaseViewModel() {
    private val interestedUsersId = arrayListOf("0123456789", "0987654321", "0111111111")

    val liveDiaries = MutableLiveData(ArrayList<Diary>())
    var lastDiaries: ArrayList<Diary>? = null

    val liveStoryGroups: MutableLiveData<List<StoryGroup>> = MutableLiveData(ArrayList())

    val liveIsAnyStory: MutableLiveData<Boolean> = MutableLiveData(false)
    var idUserMap: HashMap<String, User>? = null

    init {
        database.getUsers(interestedUsersId) { users ->
            val map = hashMapOf<String, User>()
            users.forEach { user ->
                map[user.id!!] = user
            }
            idUserMap = map
            updateLiveDiaries()
        }
        loadMoreDiaries()
        refreshRecentStoryGroup()
        listenForNewStory()
    }

    fun refreshRecentStoryGroup() {
        database.getUsersRecentStoryGroup(
                interestedUsersId
        ) { storyGroups ->
            liveStoryGroups.value = storyGroups.sortedWith(Comparator { o1, o2 ->
                val curUserId = sessionManager.curUser!!.id
                return@Comparator when {
                    o1.ownerId == curUserId -> {
                        -1
                    }
                    o2.ownerId == curUserId -> {
                        1
                    }
                    else -> o2.createdTime!!.compareTo(o1.createdTime!!)
                }
            })
        }
    }

    private fun listenForNewStory() {
        listenerRegistrations.add(
                database.addUserLastStoryCreatedTimeListener(sessionManager.curUser!!.id!!) {
                    liveIsAnyStory.value = it != null && it > System.currentTimeMillis() - Constants.DEFAULT_STORY_LIVE_TIME
                }
        )
    }

    fun refreshRecentDiaries(callback: (() -> Unit)? = null) {
        upperBoundTime = null
        liveDiaries.value!!.clear()
        loadMoreDiaries(callback)
    }

    private fun updateLiveDiaries() {
        if (idUserMap != null && lastDiaries != null && lastDiaries!!.isNotEmpty()) {
            var user: User
            lastDiaries!!.forEach { diary ->
                user = idUserMap!![diary.ownerId!!]!!
                diary.ownerName = user.name
                diary.ownerAvatarUrl = user.avatarUrl
            }
            liveDiaries.value = liveDiaries.value!!.apply { addAll(lastDiaries!!) }
        }
    }

    private var upperBoundTime:Long? = null
    private var isLoading = false

    fun loadMoreDiaries(callback: (() -> Unit)? = null){
        isLoading = true

        database.getUsersRecentDiaries(
                usersId = interestedUsersId,
                upperCreatedTimeLimit = upperBoundTime,
                numberLimit = 15
        ) { diaries ->
            lastDiaries = diaries
            upperBoundTime = diaries.minBy { it.createdTime!! }?.createdTime
            Log.d(TAG, "more upper: $upperBoundTime")
            updateLiveDiaries()
            isLoading = false
            callback?.invoke()
        }

        Log.d(TAG, "load more diaries")
    }

    fun shouldLoadMoreDiaries(): Boolean {
        return upperBoundTime != null && !isLoading
    }
}