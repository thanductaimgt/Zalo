package vng.zalo.tdtai.zalo.repo

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.message.Message
import vng.zalo.tdtai.zalo.model.room.Room
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

//@Singleton
class FirebaseStorage @Inject constructor(
        private val resourceManager: ResourceManager,
        private val utils: Utils
):Storage {
    private val firebaseStorage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    private fun getReference(path: String): StorageReference {
        return firebaseStorage.reference.child(path)
    }

    override fun addResourceAndGetDownloadUrl(localPath: String, storagePath: String, fileType: Int, fileSize: Long, onProgressChange: ((progress: Int) -> Unit)?, onComplete: ((url: String) -> Unit)?) {
        var resourceSize = fileSize

        val inputStream = when (fileType) {
            Message.TYPE_IMAGE -> {
                var bitmap = getScaledDownBitmap(
                        resourceManager.getImageBitmap(localPath)
                )

                val rotationDegree = resourceManager.getImageRotation(localPath)
                if (rotationDegree != 0) {
                    bitmap = rotateImage(bitmap, rotationDegree)
                }

                //transfer to input stream
                val bos = ByteArrayOutputStream()
                bitmap.compress(Constants.IMAGE_COMPRESS_FORMAT, Constants.IMAGE_COMPRESS_QUALITY, bos)
                val bitmapData: ByteArray = bos.toByteArray()

                resourceSize = bitmapData.size.toLong()

                ByteArrayInputStream(bitmapData)
            }
            else -> utils.getInputStream(localPath)
        }

        val fileStorageRef = firebaseStorage.reference.child(storagePath)

        fileStorageRef.putStream(inputStream)
                .addOnProgressListener {
                    if (resourceSize != 0L) {
                        onProgressChange?.invoke((it.bytesTransferred * 100 / resourceSize).toInt())
                    }
                }.continueWithTask {
                    var isSuccess = false
                    utils.assertTaskSuccess(it, TAG, "addFileAndGetDownloadUrl") {
                        isSuccess = true
                    }
                    if (isSuccess) {
                        getReference(storagePath).downloadUrl
                    } else {
                        null
                    }
                }.addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "addFile") { uri ->
                        onComplete?.invoke(uri.toString())
                    }
                }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    private fun getScaledDownBitmap(bitmap: Bitmap): Bitmap {
        val scaleRatio = max(min(bitmap.width, bitmap.height) / 1000f, 1f)
        val dstWidth = (bitmap.width / scaleRatio).toInt()
        val dstHeight = (bitmap.height / scaleRatio).toInt()

        return Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false)
    }

    override fun getStickerSetUrls(bucketName: String, callback: ((List<String>) -> Unit)?) {
        Log.d(TAG, firebaseStorage.reference.child("${FOLDER_STICKER_SETS}/$bucketName/").path)
        firebaseStorage.reference.child("${FOLDER_STICKER_SETS}/$bucketName/").listAll().addOnCompleteListener { task ->
            utils.assertTaskSuccessAndResultNotNull(task, TAG, "getStickerSetUrls.listAll") { listResult ->
                val stickerUrlsTasks = ArrayList<Task<Uri>>()
                listResult.items.forEach {
                    stickerUrlsTasks.add(it.downloadUrl)
                }

                Tasks.whenAll(stickerUrlsTasks).addOnCompleteListener {
                    val stickerUrls = ArrayList<String>()
                    stickerUrlsTasks.forEachIndexed { index, task ->
                        utils.assertTaskSuccessAndResultNotNull(task, TAG, "getStickerSetUrls.task[$index]") { uri ->
                            stickerUrls.add(uri.toString())
                        }
                    }

                    callback?.invoke(stickerUrls)
                }
            }
        }
    }

    override fun addStickerSet(name: String, localPaths: List<String>, callback: ((bucketName: String) -> Unit)?) {
        /*
         */
        val bucketName = utils.getStickerBucketNameFromName(name)
        callback?.invoke(bucketName)
    }

    override fun deleteAttachedDataFromStorage(roomId: String, onSuccess: (() -> Unit)?) {
        val fileStoragePath = getRoomDataStoragePath(roomId)

        firebaseStorage.reference.child(fileStoragePath).delete().addOnCompleteListener {
            utils.assertTaskSuccess(it, TAG, "deleteAttachedDataFromStorage") { onSuccess?.invoke() }
        }
    }

//    fun generateNewStoragePath(context: Context, room: Room, localUriString: String, createdTime: Long):String{
//        val extension = if (Utils.isContentUri(localUriString)) {
//            val localUri = Uri.parse(localUriString)
//            MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(localUri))
//        } else {
//            Utils.getFileExtension(localUriString)
//        }
//
//        return getMessageDataStoragePath(room.id!!,createdTime)
//    }

    override fun deleteRoomAvatarAndData(room: Room): List<Task<Void>> {
        val tasks = ArrayList<Task<Void>>()
        if (room.type == Room.TYPE_GROUP) {
            val avatarStoragePath = getRoomAvatarStoragePath(room.id!!)
            tasks.add(getReference(avatarStoragePath).delete())
        }

        val dataStoragePath = getRoomDataStoragePath(room.id!!)
        getReference(dataStoragePath).delete()

        return tasks
    }

    override fun getRoomAvatarStoragePath(roomId: String): String {
        return "${FOLDER_ROOM_AVATARS}/$roomId"
    }

    private fun getRoomDataStoragePath(roomId: String): String {
        return "${FOLDER_ROOM_DATA}/${roomId}"
    }

    override fun getMessageDataStoragePath(roomId: String, message: Message): String {
        return "${getRoomDataStoragePath(roomId)}/file_${message.createdTime!!}"
    }

    companion object{
//        private const val FOLDER_USER_AVATARS = "user_avatars"
        private const val FOLDER_ROOM_AVATARS = "room_avatars"
        private const val FOLDER_ROOM_DATA = "room_data"
        private const val FOLDER_STICKER_SETS = "sticker_sets"
    }
}