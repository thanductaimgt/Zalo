package vng.zalo.tdtai.zalo.ui.create_post

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.repository.Storage
import vng.zalo.tdtai.zalo.util.TAG
import javax.inject.Inject

class CreatePostViewModel @Inject constructor() : BaseViewModel() {
    val liveDiary = MutableLiveData(Diary(
            id = database.getNewPostId(),
            ownerId = sessionManager.curUser!!.id
    ))

    fun createDiary(callback: (isSuccess: Boolean) -> Unit) {
        val storagePath = storage.getPostStoragePath(liveDiary.value!!)

        var uploadCount = 0
        var uploadedCount = 0
        val totalCount = liveDiary.value!!.medias.size
        val checkAllUploadCompletedAndCreateDiary = {
            if (uploadCount == totalCount) {
                if (uploadedCount == uploadCount) {
                    database.createPost(liveDiary.value!!, callback)
                } else {
                    Log.d(TAG, "uploadCount: $uploadCount, uploadedCount: $uploadedCount, totalCount: $totalCount")
                }
            } else {
                callback(false)
            }
        }

        if (liveDiary.value!!.medias.isNotEmpty()) {
            liveDiary.value!!.medias.forEachIndexed { index, media ->
                storage.addFileAndGetDownloadUrl(
                        localPath = media.uri!!,
                        storagePath = "$storagePath/$index",
                        fileType = if (media is ImageMedia) Storage.FILE_TYPE_IMAGE else Storage.FILE_TYPE_VIDEO,
                        onComplete = { url ->
                            liveDiary.value!!.medias[index].uri = url
                            uploadedCount++
                            checkAllUploadCompletedAndCreateDiary()
                        }
                )
                uploadCount++
            }
        } else {
            checkAllUploadCompletedAndCreateDiary()
        }
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