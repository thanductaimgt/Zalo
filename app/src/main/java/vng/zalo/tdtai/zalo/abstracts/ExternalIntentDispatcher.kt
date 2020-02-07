package vng.zalo.tdtai.zalo.abstracts

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils

object ExternalIntentDispatcher {
    fun dispatchChooserIntent(activity: Activity, requestCode: Int, chooserType: Int, allowMultiple: Boolean) {
        val mimeType: String
        val mediaStoreUri: Uri
        when (chooserType) {
            CHOOSER_TYPE_IMAGE -> {
                mimeType = "image/*"
                mediaStoreUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            CHOOSER_TYPE_VIDEO -> {
                mimeType = "video/*"
                mediaStoreUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            CHOOSER_TYPE_AUDIO -> {
                mimeType = "audio/*"
                mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            else -> {
                mimeType = "*/*"
                mediaStoreUri = MediaStore.Files.getContentUri("external")
            }
        }

        val getIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        }

//        val pickIntent = Intent(Intent.ACTION_PICK, mediaStoreUri).apply { type = mimeType }

        val chooserIntent = Intent.createChooser(getIntent, activity.getString(R.string.label_choose_from))
//                .apply {
//            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
//        }

        activity.startActivityForResult(chooserIntent, requestCode)
    }

    //return picture uri
    fun dispatchCaptureIntent(activity: Activity, requestCode: Int, captureType: Int, callback: ((localPath: String?) -> Unit)? = null) {
        val fileType: Int
        val action: String
        when (captureType) {
            CAPTURE_TYPE_IMAGE -> {
                fileType = ResourceManager.FILE_TYPE_IMAGE
                action = MediaStore.ACTION_IMAGE_CAPTURE
            }
            else -> {
                action = MediaStore.ACTION_VIDEO_CAPTURE
                fileType = ResourceManager.FILE_TYPE_VIDEO
            }
        }

        Intent(action).also { captureIntent ->
            // Ensure that there's a camera activity to handle the intent
            captureIntent.resolveActivity(activity.packageManager)?.let {
                // Create the File where the photo should go
                ResourceManager.createTempFile(activity, fileType) { file ->
                    Utils.assertNotNull(file, TAG, "dispatchCaptureIntent") { fileNotNull ->
                        val fileRealUriString: String = fileNotNull.absolutePath
                        val fileContentUri: Uri = ResourceManager.getContentUri(activity, fileRealUriString)

                        captureIntent.apply {
                            putExtra(MediaStore.EXTRA_OUTPUT, fileContentUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        }

                        activity.packageManager.queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY).apply {
                            forEach { resolveInfo ->
                                val packageName = resolveInfo.activityInfo.packageName
                                activity.grantUriPermission(packageName, fileContentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }

                        activity.startActivityForResult(captureIntent, requestCode)

                        callback?.invoke(fileRealUriString)
                    }
                }
            }
        }
    }

    const val CHOOSER_TYPE_IMAGE = 0
    const val CHOOSER_TYPE_VIDEO = 1
    const val CHOOSER_TYPE_FILE = 2
    const val CHOOSER_TYPE_AUDIO = 3

    const val CAPTURE_TYPE_IMAGE = 10
    const val CAPTURE_TYPE_VIDEO = 11
    const val CAPTURE_TYPE_AUDIO = 12
}