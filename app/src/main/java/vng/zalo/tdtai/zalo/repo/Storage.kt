package vng.zalo.tdtai.zalo.repo

import com.google.android.gms.tasks.Task
import vng.zalo.tdtai.zalo.model.message.Message
import vng.zalo.tdtai.zalo.model.room.Room
import javax.inject.Singleton

@Singleton
interface Storage {
    fun addResourceAndGetDownloadUrl(localPath: String, storagePath: String, fileType: Int, fileSize: Long = -1, onProgressChange: ((progress: Int) -> Unit)? = null, onComplete: ((url: String) -> Unit)? = null)

    fun getStickerSetUrls(bucketName: String, callback: ((List<String>) -> Unit)?)

    fun addStickerSet(name: String, localPaths: List<String>, callback: ((bucketName: String) -> Unit)?)

    fun deleteAttachedDataFromStorage(roomId: String, onSuccess: (() -> Unit)? = null)

    fun deleteRoomAvatarAndData(room: Room): List<Task<Void>>

    fun getRoomAvatarStoragePath(roomId: String): String

    fun getMessageDataStoragePath(roomId: String, message: Message): String
}