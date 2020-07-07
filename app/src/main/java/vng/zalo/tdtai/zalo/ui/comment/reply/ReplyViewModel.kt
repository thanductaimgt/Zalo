package vng.zalo.tdtai.zalo.ui.comment.reply

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.Comment
import vng.zalo.tdtai.zalo.data_model.User
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.data_model.post.Post
import vng.zalo.tdtai.zalo.repository.Storage
import vng.zalo.tdtai.zalo.util.TAG
import javax.inject.Inject


class ReplyViewModel @Inject constructor(private val post: Post, private val comment: Comment) : BaseViewModel() {
    private var repliesListenerRegistration: ListenerRegistration? = null
    private var isNewRepliesSubmitted: Boolean = false
    private var isFirstLoad: Boolean = true
    val liveIsLoading = MutableLiveData(false)
    private var lastBoundReply: Comment? = null

    lateinit var myReply:Comment

    val liveReplies = MutableLiveData(ArrayList<Comment>())

    var idUserMap: HashMap<String, User>? = null

    init {
        resetReply()
        loadMoreReplies()
    }

    fun resetReply(){
        myReply = Comment(
                id = database.getNewCommentId(post.id!!),
                postId = post.id,
                replyToId = if (comment.replyToId == null) comment.id else comment.replyToId,
                ownerId = sessionManager.curUser!!.id,
                ownerName = sessionManager.curUser!!.name,
                ownerAvatarUrl = sessionManager.curUser!!.avatarUrl
        )
    }

    fun loadMoreReplies(boundComment: Comment? = null) {
        isFirstLoad = false
        liveIsLoading.value = true

        database.getComments(
                postId = comment.postId!!,
                replyToId = comment.id,
                boundValue = boundComment?.createdTime,
                numberLimit = REPLY_LOAD_NUM
        ) { replies ->
            lastBoundReply = replies.lastOrNull()?.also { newBoundComment ->
                loadOwners(replies) {
                    attachUsersInfo(replies)
                    liveReplies.value = liveReplies.value!!.apply { addAll(replies) }
                }

                isNewRepliesSubmitted = true

                reObserveReplies(newBoundComment)
            }
            if (lastBoundReply == null) {
                liveIsLoading.value = false
            }
        }

        Log.d(TAG, "loadMoreReplies")
    }

    private fun reObserveReplies(boundReply: Comment) {
        repliesListenerRegistration?.let {
            it.remove()
            listenerRegistrations.remove(it)
        }

        repliesListenerRegistration = database.addNewCommentsListener(
                postId = comment.postId!!,
                replyToId = comment.id!!,
                boundValue = boundReply.createdTime!!
        ) { newReplies ->
            if (isNewRepliesSubmitted) {
                isNewRepliesSubmitted = false
            } else {
//                val newReplies = newReplies.filter { reply -> liveReplies.value!!.all { it.id != reply.id } } as ArrayList
                loadOwners(newReplies) {
                    attachUsersInfo(newReplies)
//                    liveReplies.value = newReplies
                    liveReplies.value = liveReplies.value!!.apply { addAll(newReplies) }
                }
            }
        }
        listenerRegistrations.add(repliesListenerRegistration!!)
    }

    private fun loadOwners(replies: ArrayList<Comment>, callback: (() -> Unit)? = null) {
        val usersId = replies.filter { !(idUserMap?.contains(it.ownerId) ?: false) }
                .map { it.ownerId!! }
        database.getUsers(usersId) { users ->
            val map = if (idUserMap == null) hashMapOf() else idUserMap!!
            users.forEach { user ->
                map[user.id!!] = user
            }
            idUserMap = map

            liveIsLoading.value = false
            callback?.invoke()
        }
    }

    private fun attachUsersInfo(replies:ArrayList<Comment>){
        var user: User
        replies.forEach { reply ->
            user = idUserMap!![reply.ownerId!!]!!
            reply.ownerName = user.name
            reply.ownerAvatarUrl = user.avatarUrl
        }
    }

    fun shouldLoadMoreComments(): Boolean {
        return lastBoundReply != null && !liveIsLoading.value!!
    }

    fun sendReply(callback: ((isSuccess: Boolean) -> Unit)? = null) {
        val replyStoragePath = storage.getCommentStoragePath(post, myReply)
        val myReply = myReply
        if (myReply.media != null) {
            storage.addFileAndGetDownloadUrl(
                    localPath = myReply.media!!.uri!!,
                    storagePath = replyStoragePath,
                    fileType = if (myReply.media is ImageMedia) Storage.FILE_TYPE_IMAGE else Storage.FILE_TYPE_VIDEO,
                    onComplete = { downloadUrl ->
                        myReply.media!!.uri = downloadUrl
                        database.addComment(myReply, callback)
                    }
            )
        } else {
            database.addComment(myReply, callback)
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

                    val duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000
                    val width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                    val height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
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
        const val REPLY_LOAD_NUM: Long = 20
    }
}