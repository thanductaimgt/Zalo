package vng.zalo.tdtai.zalo.zalo.networks

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.Utils
import java.io.File

class Storage {
    companion object {
        private val firebaseStorage: FirebaseStorage
            get() = FirebaseStorage.getInstance()

        private val defaultBucketRef: StorageReference
            get() = firebaseStorage.reference

        fun addFile(localPath: String, storagePath: String, callback: (() -> Unit)? = null) {
            val localFileUri = Uri.fromFile(File(localPath))

            val fileStorageRef = defaultBucketRef.child(storagePath)

            fileStorageRef.putFile(localFileUri)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback?.invoke()
                        } else {
                            Log.e(Utils.getTag(object {}), "task fail")
                        }
                    }
        }

        fun getStickerSetUrls(bucket_name: String, callback: ((List<String>) -> Unit)?) {
            defaultBucketRef.child("${Constants.STICKER_SETS_PATH}/$bucket_name/").listAll().addOnCompleteListener { task ->
                Utils.assertTaskSuccessAndResultNotNull(
                        tag = "getStickerSetUrls",
                        task = task
                ) {
                    val stickerUrlsTasks = ArrayList<Task<Uri>>()
                    task.result!!.items.forEach {
                        stickerUrlsTasks.add(it.downloadUrl)
                    }

                    Tasks.whenAll(stickerUrlsTasks).addOnCompleteListener {
                        val stickerUrls = ArrayList<String>()
                        stickerUrlsTasks.forEach {
                            Utils.assertTaskSuccessAndResultNotNull(
                                    tag = "Tasks.whenAll",
                                    task = it
                            ) {
                                stickerUrls.add(it.result!!.toString())
                            }
                        }

                        callback?.invoke(stickerUrls)
                    }
                }
            }
        }
    }
}