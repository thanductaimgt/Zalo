package com.mgt.zalo.manager

import android.media.MediaMetadataRetriever
import android.provider.OpenableColumns
import android.util.Log
import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.data_model.message.*
import com.mgt.zalo.data_model.room.Room
import com.mgt.zalo.repository.Database
import com.mgt.zalo.repository.Storage
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.Utils
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageManager @Inject constructor(
        private val application: ZaloApplication,
        private val database: Database,
        private val storage: Storage,
        private val sessionManager: SessionManager,
        private val resourceManager: ResourceManager,
        private val sharedPrefsManager: SharedPrefsManager,
        private val utils: Utils
) {
    fun addNewMessagesToFirestore(room: Room, contents: List<String>, messageType: Int, observer: Observer<Message>?=null) {
        Observable.create<Message> { emitter ->
                    addNewMessagesToFirestoreInternal(room, contents, messageType, emitter)
                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .also {
                    if (observer != null) {
                        it.subscribe(observer)
                    } else {
                        it.subscribe()
                    }
                }
    }

    private fun addNewMessagesToFirestoreInternal(room: Room, contents: List<String>, messageType: Int, emitter: Emitter<Message>) {
        var curTimeStamp = System.currentTimeMillis()

        contents.forEach { content ->
            val message = when (messageType) {
                Message.TYPE_CALL -> {
                    createCallMessage(room, content, curTimeStamp)
                }
                Message.TYPE_STICKER -> {
                    createStickerMessage(room, content, curTimeStamp)
                }
                Message.TYPE_TEXT -> {
                    createTextMessage(room, content, curTimeStamp)
                }
                else -> {
                    createResourceMessage(room, content, curTimeStamp, messageType)
                }
            }

            emitter.onNext(message)

            if (message is MediaMessage) {
                uploadResourceAndAddMessage(room, message, emitter)
            } else {
                database.addNewMessageAndUpdateUsersRoom(
                        curRoom = room,
                        newMessage = message
                ) {
                    emitter.onNext(message.clone().apply { isSent = true })
                    emitter.onComplete()
                }
            }

            curTimeStamp++
        }
    }

    private fun createTextMessage(room: Room, content: String, createdTime: Long): TextMessage {
        return TextMessage(
                id = database.getNewMessageId(room.id!!),
                createdTime = createdTime,
                senderId = sessionManager.curUser!!.id,
                senderAvatarUrl = sessionManager.curUser!!.avatarUrl,
                content = content
        )
    }

    private fun createResourceMessage(room: Room, content: String, createdTime: Long, messageType: Int): MediaMessage {
        @Suppress("NAME_SHADOWING")
        var messageType = messageType

        if (messageType == Message.TYPE_FILE) {
            val mimeType = application.contentResolver.getType(utils.getUri(content))
            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    messageType = Message.TYPE_IMAGE
                } else if (mimeType.startsWith("video/")) {
                    messageType = Message.TYPE_VIDEO
                }
            }
        }

        val message = when (messageType) {
            Message.TYPE_IMAGE -> {
                createImageMessage(room, content, createdTime)
            }
            Message.TYPE_VIDEO -> {
                createVideoMessage(room, content, createdTime)
            }
            else -> {//Message.TYPE_FILE
                createFileMessage(room, content, createdTime)
            }
        }

        val uri = utils.getUri(content)
        if (resourceManager.isContentUri(content)) {
//            if (DocumentsContract.isDocumentUri(context, uri)) {
            with(application.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)) {
                this?.let { cursor ->
                    if (cursor.moveToNext()) {
                        message.size = cursor.getLong(0)
                    }
                }
            }
        } else {
            message.size = File(content).length()
        }
        return message
    }

    private fun createImageMessage(room: Room, content: String, createdTime: Long): ImageMessage {
        val ratio = resourceManager.getImageDimension(content)

        return ImageMessage(
                id = database.getNewMessageId(room.id!!),
                createdTime = createdTime,
                senderId = sessionManager.curUser!!.id,
                senderAvatarUrl = sessionManager.curUser!!.avatarUrl,
                //todo(remove comment)
                url = content,
//                    url = "https://hips.hearstapps.com/hmg-prod.s3.amazonaws.com/images/nature-quotes-1557340276.jpg?crop=0.666xw:1.00xh;0.168xw,0&resize=640:*",
                ratio = ratio
        )
    }

    private fun createFileMessage(room: Room, content: String, createdTime: Long): FileMessage {
        // resolve file info
        var fileName: String? = null

        when {
            resourceManager.isContentUri(content) -> {
                val localUri = utils.getUri(content)

                with(application.contentResolver.query(localUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)) {
                    if (this != null && moveToFirst()) {
                        fileName = getString(0)
                    } else {
                        Log.e(this@MessageManager.TAG, "Fail to resolve info: $content")
                    }
                }
            }
            else -> {
                val file = File(content)

                fileName = file.name
            }
        }

        return FileMessage(
                id = database.getNewMessageId(room.id!!),
                createdTime = createdTime,
                senderId = sessionManager.curUser!!.id,
                senderAvatarUrl = sessionManager.curUser!!.avatarUrl,
                //todo(remove comment)
                url = content,
//                    url = "https://drive.google.com/uc?export=download&id=1FFPymC0CYG0TRhh8x19gjC-SztXsRY0v",
                fileName = fileName
        )
    }

    private fun createVideoMessage(room: Room, content: String, createdTime: Long): VideoMessage {
        val retriever = resourceManager.getMetadataRetriever(content)

        var width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!)
        var height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!)
        val duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!) / 1000
        val rotation = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)!!)
        if (rotation == 90 || rotation == 270) {
            val temp = width
            width = height
            height = temp
        }

        retriever.release()

        val ratio = "$width:$height"

        return VideoMessage(
                id = database.getNewMessageId(room.id!!),
                createdTime = createdTime,
                senderId = sessionManager.curUser!!.id,
                senderAvatarUrl = sessionManager.curUser!!.avatarUrl,
                duration = duration,
                url = content,
                ratio = ratio
                //todo(remove comment)
//                    url = "https://drive.google.com/uc?export=download&id=1FFPymC0CYG0TRhh8x19gjC-SztXsRY0v"
        )
    }

    private fun uploadResourceAndAddMessage(
            room: Room,
            message: MediaMessage,
            emitter: Emitter<Message>) {

        val fileStoragePath = storage.getMessageDataStoragePath(room.id!!, message)

//        //todo
//        incProg(message, emitter)
        //
        val fileType = when(message){
            is ImageMessage->Storage.FILE_TYPE_IMAGE
            else->Storage.FILE_TYPE_VIDEO
        }

        var newMessage = message
        storage.addFileAndGetDownloadUrl(message.url, fileStoragePath, fileType,
                fileSize = message.size,
                onProgressChange = {
                    newMessage = (newMessage.clone() as MediaMessage).apply { uploadProgress = it }
                    emitter.onNext(newMessage)
                },
                onComplete = { downloadUrl ->
                    newMessage = (newMessage.clone() as MediaMessage).apply {
                        uploadProgress = null
//                        url = downloadUrl
                    }
                    emitter.onNext(newMessage)

                    val localResourceUri = newMessage.url

                    newMessage = (newMessage.clone() as MediaMessage).apply { url = downloadUrl }
                    database.addNewMessageAndUpdateUsersRoom(
                            curRoom = room,
                            newMessage = newMessage
                    ) {
                        emitter.onNext(newMessage.clone().apply { isSent = true })
                        emitter.onComplete()
                    }

                    if (newMessage is VideoMessage) {
                        val thumbUriString = sharedPrefsManager.getVideoThumbCacheUri(localResourceUri)
                        thumbUriString?.let { sharedPrefsManager.setVideoThumbCacheUri(downloadUrl, it) }
                    }
                })
    }

//    private fun incProg(message: ResourceMessage, emitter: Emitter<Message>) {
//        //todo
//        val messageCopy = message.clone() as ResourceMessage
//
//        when(messageCopy.uploadProgress) {
//            null -> {
//                messageCopy.uploadProgress = 1
//            }
//            100 -> {
//                messageCopy.uploadProgress = null
//            }
//            else -> {
//                messageCopy.uploadProgress = messageCopy.uploadProgress!! + 1
//            }
//        }
//        emitter.onNext(messageCopy)
//
//        if (messageCopy.uploadProgress != null && messageCopy.uploadProgress!! <= 100) {
//            Handler(Looper.getMainLooper()).postDelayed(
//                    {
//                        incProg(messageCopy, emitter)
//                    },
//                    100
//            )
//        }
//    }

    private fun createCallMessage(room: Room, content: String, createdTime: Long): CallMessage {
        val data = content.split('.')

        val callType = data[0].toInt()
        val callTime = data[1].toInt()
        val isMissed = data[2].toBoolean()
        val isCanceled = data[3].toBoolean()

        return CallMessage(
                id = database.getNewMessageId(room.id!!),
                createdTime = createdTime,
                senderId = sessionManager.curUser!!.id,
                senderAvatarUrl = sessionManager.curUser!!.avatarUrl,
                callType = callType,
                callTime = callTime,
                isMissed = isMissed,
                isCanceled = isCanceled
        )
    }

    private fun createStickerMessage(room: Room, content: String, createdTime: Long): StickerMessage {
        return StickerMessage(
                id = database.getNewMessageId(room.id!!),
                createdTime = createdTime,
                senderId = sessionManager.curUser!!.id,
                senderAvatarUrl = sessionManager.curUser!!.avatarUrl,
                url = content
        )
    }

    fun deleteMessageAndAttachedData(roomId: String, message: Message, onSuccess: (() -> Unit)?=null) {
        var isPrevTaskSuccess = false
        database.deleteMessageFromFirestore(roomId, message.id!!) {
            if (isPrevTaskSuccess) {
                onSuccess?.invoke()
            } else {
                isPrevTaskSuccess = true
            }
        }
        storage.deleteAttachedDataFromStorage(roomId) {
            if (isPrevTaskSuccess) {
                onSuccess?.invoke()
            } else {
                isPrevTaskSuccess = true
            }
        }
    }
}