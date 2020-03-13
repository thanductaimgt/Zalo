package vng.zalo.tdtai.zalo.managers

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import javax.inject.Inject

interface ExternalIntentManager {
    fun dispatchChooserIntent(activity: Activity, requestCode: Int, chooserType: Int, allowMultiple: Boolean)
    fun dispatchCaptureIntent(activity: Activity, requestCode: Int, captureType: Int, callback: ((localPath: String?) -> Unit)? = null)

    companion object {
        const val CHOOSER_TYPE_IMAGE = 0
        const val CHOOSER_TYPE_VIDEO = 1
        const val CHOOSER_TYPE_FILE = 2
        const val CHOOSER_TYPE_AUDIO = 3

        const val CAPTURE_TYPE_IMAGE = 10
        const val CAPTURE_TYPE_VIDEO = 11
//    const val CAPTURE_TYPE_AUDIO = 12
    }
}

class ExternalIntentManagerImpl @Inject constructor(
        private val resourceManager: ResourceManager,
        private val utils: Utils
) : ExternalIntentManager {
    override fun dispatchChooserIntent(activity: Activity, requestCode: Int, chooserType: Int, allowMultiple: Boolean) {
        val mimeType = when (chooserType) {
            ExternalIntentManager.CHOOSER_TYPE_IMAGE -> "image/*"
            ExternalIntentManager.CHOOSER_TYPE_VIDEO -> "video/*"
            ExternalIntentManager.CHOOSER_TYPE_AUDIO -> "audio/*"
            else -> "*/*"
        }

        val getIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        }

        val chooserIntent = Intent.createChooser(getIntent, activity.getString(R.string.label_choose_from))
        activity.startActivityForResult(chooserIntent, requestCode)
    }

    //return picture uri
    override fun dispatchCaptureIntent(activity: Activity, requestCode: Int, captureType: Int, callback: ((localPath: String?) -> Unit)?) {
        val fileType: Int
        val action: String
        when (captureType) {
            ExternalIntentManager.CAPTURE_TYPE_IMAGE -> {
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
                resourceManager.createTempFile(fileType) { file ->
                    utils.assertNotNull(file, TAG, "dispatchCaptureIntent") { fileNotNull ->
                        val fileRealUriString: String = fileNotNull.absolutePath
                        val fileContentUri: Uri = resourceManager.getContentUri(fileRealUriString)

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
}