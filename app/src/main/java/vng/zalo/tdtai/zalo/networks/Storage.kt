package vng.zalo.tdtai.zalo.networks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import vng.zalo.tdtai.zalo.abstracts.ExternalSourceManager
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min


object Storage {
    private val firebaseStorage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    private fun getReference(path: String): StorageReference {
        return firebaseStorage.reference.child(path)
    }

    fun addResourceAndGetDownloadUrl(context: Context, localPath: String, storagePath: String, fileType: Int, fileSize: Long = -1, onProgressChange: ((progress: Int) -> Unit)? = null, onComplete: ((url: String) -> Unit)? = null) {
        var resourceSize = fileSize

        val inputStream = when (fileType) {
            Message.TYPE_IMAGE -> {
                var bitmap = getScaledDownBitmap(
                        ExternalSourceManager.getImageBitmap(context, localPath)
                )

                val rotationDegree = ExternalSourceManager.getImageRotation(context, localPath)
                if (rotationDegree != 0) {
                    bitmap = rotateImage(bitmap, rotationDegree)
                }

                //transfer to input stream
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75 /*ignored for PNG*/, bos)
                val bitmapData: ByteArray = bos.toByteArray()

                resourceSize = bitmapData.size.toLong()

                ByteArrayInputStream(bitmapData)
            }
            else -> Utils.getInputStream(context, localPath)
        }

        val fileStorageRef = firebaseStorage.reference.child(storagePath)

        fileStorageRef.putStream(inputStream)
                .addOnProgressListener {
                    onProgressChange?.invoke((it.bytesTransferred * 100 / resourceSize).toInt())
                }.continueWithTask {
                    var isSuccess = false
                    Utils.assertTaskSuccess(it, TAG, "addFileAndGetDownloadUrl") {
                        isSuccess = true
                    }
                    if (isSuccess) {
                        getReference(storagePath).downloadUrl
                    } else {
                        null
                    }
                }.addOnCompleteListener { task ->
                    Utils.assertTaskSuccessAndResultNotNull(task, TAG, "addFile") { uri ->
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

    fun getStickerSetUrls(bucketName: String, callback: ((List<String>) -> Unit)?) {
        Log.d(TAG, firebaseStorage.reference.child("${FOLDER_STICKER_SETS}/$bucketName/").path)
        firebaseStorage.reference.child("${FOLDER_STICKER_SETS}/$bucketName/").listAll().addOnCompleteListener { task ->
            Utils.assertTaskSuccessAndResultNotNull(task, TAG, "getStickerSetUrls.listAll") { listResult ->
                val stickerUrlsTasks = ArrayList<Task<Uri>>()
                listResult.items.forEach {
                    stickerUrlsTasks.add(it.downloadUrl)
                }

                Tasks.whenAll(stickerUrlsTasks).addOnCompleteListener {
                    val stickerUrls = ArrayList<String>()
                    stickerUrlsTasks.forEachIndexed { index, task ->
                        Utils.assertTaskSuccessAndResultNotNull(task, TAG, "getStickerSetUrls.task[$index]") { uri ->
                            stickerUrls.add(uri.toString())
                        }
                    }

                    callback?.invoke(stickerUrls)
                }
            }
        }
    }

    fun addStickerSet(name: String, localPaths: List<String>, callback: ((bucketName: String) -> Unit)?) {
        /*
         */
        val bucketName = Utils.getStickerBucketNameFromName(name)
        callback?.invoke(bucketName)
    }

    fun deleteAttachedDataFromStorage(roomId: String, onSuccess: (() -> Unit)? = null) {
        val fileStoragePath = getRoomDataStoragePath(roomId)

        firebaseStorage.reference.child(fileStoragePath).delete().addOnCompleteListener {
            Utils.assertTaskSuccess(it, TAG, "deleteAttachedDataFromStorage") { onSuccess?.invoke() }
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

    fun deleteRoomAvatarAndData(room: Room): List<Task<Void>> {
        val tasks = ArrayList<Task<Void>>()
        if (room.type == Room.TYPE_GROUP) {
            val avatarStoragePath = getRoomAvatarStoragePath(room.id!!)
            tasks.add(getReference(avatarStoragePath).delete())
        }

        val dataStoragePath = getRoomDataStoragePath(room.id!!)
        getReference(dataStoragePath).delete()

        return tasks
    }

    fun getRoomAvatarStoragePath(roomId: String): String {
        return "${FOLDER_ROOM_AVATARS}/$roomId"
    }

    private fun getRoomDataStoragePath(roomId: String): String {
        return "${FOLDER_ROOM_DATA}/${roomId}"
    }

    fun getMessageDataStoragePath(roomId: String, message: Message): String {
        return "${getRoomDataStoragePath(roomId)}/file_${message.createdTime!!.toDate().time}"
    }


    private const val FOLDER_USER_AVATARS = "user_avatars"
    private const val FOLDER_ROOM_AVATARS = "room_avatars"
    private const val FOLDER_ROOM_DATA = "room_data"
    private const val FOLDER_STICKER_SETS = "sticker_sets"
}