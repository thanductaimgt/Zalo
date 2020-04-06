package vng.zalo.tdtai.zalo.ui.home

import android.media.MediaMetadataRetriever
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.story.ImageStory
import vng.zalo.tdtai.zalo.data_model.story.VideoStory
import vng.zalo.tdtai.zalo.repository.Storage
import javax.inject.Inject

class HomeViewModel @Inject constructor() : BaseViewModel() {
    fun createStory(media: Media, callback: ((isSuccess: Boolean) -> Unit)?=null) {
        backgroundWorkManager.single({
            val createdTime = System.currentTimeMillis()

            when (media) {
                is ImageMedia -> {
                    ImageStory(
                            createdTime = createdTime,
                            imageUrl = media.uri
                    )
                }
                else -> {
                    val retriever = resourceManager.getMetadataRetriever(media.uri)

                    val duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000

                    VideoStory(
                            createdTime = createdTime,
                            duration = duration,
                            videoUrl = media.uri
                    )
                }
            }
        }, compositeDisposable, {story->
            val storyStoragePath = storage.getStoryStoragePath(story)
            val onComplete = { downloadUrl: String ->
                when (story) {
                    is ImageStory -> story.imageUrl = downloadUrl
                    is VideoStory -> story.videoUrl = downloadUrl
                }

                database.createStory(story, callback)
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
        })
    }
}