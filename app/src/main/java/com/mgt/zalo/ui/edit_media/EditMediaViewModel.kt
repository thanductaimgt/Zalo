package com.mgt.zalo.ui.edit_media

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.data_model.media.ImageMedia
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.data_model.story.ImageStory
import com.mgt.zalo.data_model.story.Story
import com.mgt.zalo.data_model.story.VideoStory
import com.mgt.zalo.repository.Storage
import javax.inject.Inject

class EditMediaViewModel @Inject constructor() : BaseViewModel() {
    fun createStory(bitmap: Bitmap, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        backgroundWorkManager.single({
            val uri = resourceManager.saveBitmap(bitmap)
            ImageStory(
                    createdTime = System.currentTimeMillis(),
                    imageUrl = uri
            )
        }, compositeDisposable, { story ->
            createStoryInternal(story, callback)
        })
    }

    fun createStory(videoUri: String, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        backgroundWorkManager.single({
            val retriever = resourceManager.getMetadataRetriever(videoUri)

            val duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000

            retriever.release()

            VideoStory(
                    createdTime = System.currentTimeMillis(),
                    duration = duration,
                    videoUrl = videoUri
            )
        }, compositeDisposable, { story ->
            createStoryInternal(story, callback)
        })
    }

    private fun createStoryInternal(story: Story, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        val storyStoragePath = storage.getStoryStoragePath(story)
        val onComplete = { downloadUrl: String ->
            var localUri: String? = ""

            when (story) {
                is ImageStory -> {
                    localUri = story.imageUrl
                    story.imageUrl = downloadUrl
                }
                is VideoStory -> {
                    localUri = story.videoUrl
                    story.videoUrl = downloadUrl
                }
            }

            database.createStory(story) { isSuccess ->
                if (isSuccess) {
                    localUri?.let { resourceManager.deleteFileOrFolder(it) }
                }
                callback?.invoke(isSuccess)
            }
        }

        when (story) {
            is ImageStory -> {
                storage.addFileAndGetDownloadUrl(
                        localPath = story.imageUrl!!,
                        storagePath = storyStoragePath,
                        fileType = Storage.FILE_TYPE_IMAGE,
                        fileSize = resourceManager.getFileSize(story.imageUrl!!),
                        onComplete = onComplete
                )
            }
            is VideoStory -> {
                storage.addFileAndGetDownloadUrl(
                        localPath = story.videoUrl!!,
                        storagePath = storyStoragePath,
                        fileType = Storage.FILE_TYPE_VIDEO,
                        fileSize = resourceManager.getFileSize(story.videoUrl!!),
                        onComplete = onComplete
                )
            }
        }
    }

    fun createMedia(bitmap: Bitmap, callback: (media: ImageMedia) -> Unit) {
        backgroundWorkManager.single({
            val uri = resourceManager.saveBitmap(bitmap)
            val ratio = resourceManager.getImageDimension(uri)
            ImageMedia(uri, ratio)
        }, compositeDisposable, {
            callback(it)
        })
    }

    fun createMedia(videoUri: String, callback: (media: VideoMedia) -> Unit) {
        backgroundWorkManager.single({
            val retriever = resourceManager.getMetadataRetriever(videoUri)

            val duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000
            val width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
            val height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            val ratio = "$width:$height"

            retriever.release()

            VideoMedia(videoUri, ratio, duration = duration)
        }, compositeDisposable, {
            callback(it)
        })
    }
}