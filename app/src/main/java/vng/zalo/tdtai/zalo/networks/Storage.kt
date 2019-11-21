package vng.zalo.tdtai.zalo.networks

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import java.io.File

class Storage {
    companion object {
        private val firebaseStorage: FirebaseStorage
            get() = FirebaseStorage.getInstance()

        private fun getReference(path: String): StorageReference {
            return firebaseStorage.reference.child(path)
        }

//        fun addFile(localPath: String, storagePath: String, callback: (() -> Unit)? = null) {
//            val localFileUri = Uri.fromFile(File(localPath))
//
//            val fileStorageRef = firebaseStorage.reference.child(storagePath)
//
//            fileStorageRef.putFile(localFileUri)
//                    .addOnCompleteListener { task ->
//                        Utils.assertTaskSuccess(task, TAG, "addFile") {
//                            Log.d(TAG, localFileUri.toString())
//                            callback?.invoke()
//                        }
//                    }
//        }

        fun addFileAndGetDownloadUrl(context: Context, localPath: String, storagePath: String, callback: ((url: String) -> Unit)? = null) {
            val localFileUri = Uri.fromFile(File(localPath))

            val fileStorageRef = firebaseStorage.reference.child(storagePath)

//            fileStorageRef.putFile(localFileUri)
            val inputStream = Utils.getInputStream(context, localPath)
            fileStorageRef.putStream(inputStream)
                    .continueWithTask {
                var isSuccess = false
                Utils.assertTaskSuccess(it, TAG, "addFileAndGetDownloadUrl") {
                    isSuccess = true
                }
                if (isSuccess) {
                    Storage.getReference(storagePath).downloadUrl
                } else {
                    null
                }
            }.addOnCompleteListener { task ->
                Utils.assertTaskSuccessAndResultNotNull(task, TAG, "addFile") { uri ->
                    Log.d(TAG, localFileUri.toString())
                    callback?.invoke(uri.toString())
                }
            }
        }

        fun getStickerSetUrls(bucketName: String, callback: ((List<String>) -> Unit)?) {
            Log.d(TAG, firebaseStorage.reference.child("${Constants.FOLDER_STICKER_SETS}/$bucketName/").path)
            firebaseStorage.reference.child("${Constants.FOLDER_STICKER_SETS}/$bucketName/").listAll().addOnCompleteListener { task ->
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
    }
}