package com.mgt.zalo.ui.comment

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.data_model.Comment
import com.mgt.zalo.data_model.User
import com.mgt.zalo.data_model.media.ImageMedia
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.data_model.post.Post
import com.mgt.zalo.repository.Storage
import com.mgt.zalo.util.TAG
import javax.inject.Inject


class CommentViewModel @Inject constructor(private val post: Post) : BaseViewModel() {
    private var newCommentsListener: ListenerRegistration? = null
    val liveIsLoading = MutableLiveData(false)
    private var boundComment: Comment? = null
    private var shouldLoadMore = true

    lateinit var myComment:Comment

    val liveComments = MutableLiveData(ArrayList<Comment>())

    var idUserMap: HashMap<String, User>? = null

    init {
        resetMyComment()
        loadMoreComments()
    }

    fun resetMyComment() {
        myComment = Comment(
                id = database.getNewCommentId(post.id!!),
                postId = post.id,
                ownerId = sessionManager.curUser!!.id,
                ownerName = sessionManager.curUser!!.name,
                ownerAvatarUrl = sessionManager.curUser!!.avatarUrl
        )
    }

    fun loadMoreComments() {
        liveIsLoading.value = true

        database.getComments(
                postId = post.id!!,
                boundValue = boundComment?.createdTime,
                numberLimit = COMMENT_LOAD_NUM
        ) { comments ->
            val lastComment = comments.lastOrNull()
            if (lastComment != null) {
                boundComment = lastComment
                loadOwners(comments) {
                    attachUsersInfo(comments)
                    liveComments.value = liveComments.value!!.apply { addAll(comments) }
                    liveIsLoading.value = false
                }

                reObserveComments()
            } else {
                shouldLoadMore = false
                liveIsLoading.value = false
            }
        }

        Log.d(TAG, "loadMoreComments")
    }

    private fun reObserveComments() {
        newCommentsListener?.let {
            it.remove()
            listenerRegistrations.remove(it)
        }

        newCommentsListener = database.addNewCommentsListener(
                postId = post.id!!,
                boundValue = boundComment!!.createdTime!!
        ) { newComments ->
            newComments.removeAll { it.createdTime!! <= this.boundComment!!.createdTime!! }
            newComments.lastOrNull()?.let { boundComment = it }
            loadOwners(newComments) {
                attachUsersInfo(newComments)
                liveComments.value = liveComments.value!!.apply { addAll(newComments) }
            }
        }
        listenerRegistrations.add(newCommentsListener!!)
    }

    private fun loadOwners(comments: ArrayList<Comment>, callback: (() -> Unit)? = null) {
        val usersId = comments.filter { !(idUserMap?.contains(it.ownerId)?:false) }
                .map { it.ownerId!! }
        database.getUsers(usersId) { users ->
            val map = if (idUserMap == null) hashMapOf() else idUserMap!!
            users.forEach { user ->
                map[user.id!!] = user
            }
            idUserMap = map

            callback?.invoke()
        }
    }

    private fun attachUsersInfo(comments:ArrayList<Comment>){
        var user: User
        comments.forEach { comment ->
            user = idUserMap!![comment.ownerId!!]!!
            comment.ownerName = user.name
            comment.ownerAvatarUrl = user.avatarUrl
        }
    }

    fun shouldLoadMoreComments(): Boolean {
        return shouldLoadMore && !liveIsLoading.value!!
    }

    fun sendComment(callback: ((isSuccess: Boolean) -> Unit)? = null) {
        val commentStoragePath = storage.getCommentStoragePath(post, myComment)
        val myComment = myComment
        if (myComment.media != null) {
            storage.addFileAndGetDownloadUrl(
                    localPath = myComment.media!!.uri!!,
                    storagePath = commentStoragePath,
                    fileType = if (myComment.media is ImageMedia) Storage.FILE_TYPE_IMAGE else Storage.FILE_TYPE_VIDEO,
                    onComplete = { downloadUrl ->
                        myComment.media!!.uri = downloadUrl
                        database.addComment(myComment, callback)
                    }
            )
        } else {
            database.addComment(myComment, callback)
        }
    }

    fun createMedia(uri: String, mediaType: Int, callback: (media: Media) -> Unit) {
        backgroundWorkManager.single({
            when (mediaType) {
                Media.TYPE_IMAGE -> {
                    val ratio = resourceManager.getImageDimension(uri)
                    ImageMedia(uri, ratio)
                }
                else -> {
                    val retriever = resourceManager.getMetadataRetriever(uri)

                    val duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!) / 1000
                    val width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!)
                    val height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!)
                    val ratio = "$width:$height"

                    retriever.release()

                    VideoMedia(uri, ratio, duration = duration)
                }
            }
        }, compositeDisposable, { media ->
            callback(media)
        })
    }

    companion object {
        const val COMMENT_LOAD_NUM: Long = 20
    }
}