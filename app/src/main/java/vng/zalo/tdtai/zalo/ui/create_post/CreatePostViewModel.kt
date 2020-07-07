package vng.zalo.tdtai.zalo.ui.create_post

import android.content.Intent
import android.media.MediaMetadataRetriever
import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.service.UploadService
import javax.inject.Inject

class CreatePostViewModel @Inject constructor() : BaseViewModel() {
    val liveDiary = MutableLiveData(Diary(
            id = database.getNewPostId(),
            ownerId = sessionManager.curUser!!.id,
            ownerName = sessionManager.curUser!!.name,
            ownerAvatarUrl = sessionManager.curUser!!.avatarUrl
    ))

    fun createDiary() {
        application.startService(Intent(application, UploadService::class.java).apply {
            action = UploadService.ACTION_CREATE
            putExtra(UploadService.KEY_DIARY, liveDiary.value!!)
        })
    }

    fun createMedias(uris: List<String>, mediaType: Int, callback: (medias: ArrayList<Media>) -> Unit) {
        backgroundWorkManager.single({
            val medias = arrayListOf<Media>()

            uris.forEach { uri ->
                when (mediaType) {
                    Media.TYPE_IMAGE -> {
                        val ratio = resourceManager.getImageDimension(uri)
                        medias.add(ImageMedia(uri, ratio))
                    }
                    Media.TYPE_VIDEO -> {
                        val retriever = resourceManager.getMetadataRetriever(uri)

                        val duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000
                        val width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                        val height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
                        val ratio = "$width:$height"

                        retriever.release()

                        medias.add(VideoMedia(uri, ratio, duration = duration))
                    }
                }
            }
            medias
        }, compositeDisposable, { medias ->
            callback(medias)
        })
    }
}