package vng.zalo.tdtai.zalo.ui.create_post

import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.data_model.post.Post
import javax.inject.Inject

class CreatePostViewModel @Inject constructor() : BaseViewModel() {
    val liveDiary = MutableLiveData(Diary(
            id = database.getNewRoomId(),
            ownerId = sessionManager.curUser!!.id,
            ownerName = sessionManager.curUser!!.name,
            ownerAvatarUrl = sessionManager.curUser!!.avatarUrl
    ))

    fun createPost(post: Post, callback: (isSuccess: Boolean) -> Unit) {

    }
}