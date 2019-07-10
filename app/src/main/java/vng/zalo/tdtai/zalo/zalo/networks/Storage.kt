package vng.zalo.tdtai.zalo.zalo.networks

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.Utils
import java.io.File

class Storage {
    companion object {
        private val firebaseStorage: FirebaseStorage
            get() = FirebaseStorage.getInstance()

        fun addFile(localPath: String, storagePath: String, callback: (() -> Unit)? = null) {
            val localFileUri = Uri.fromFile(File(localPath))
            val fileStorageRef = firebaseStorage.reference
                    .child("${Constants.DEFAULT_STORAGE_BUCKET}/$storagePath")
            fileStorageRef.putFile(localFileUri)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback?.invoke()
                        } else {
                            Log.e(Utils.getTag(object {}), "task fail")
                        }
                    }
        }
    }
}