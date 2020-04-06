package vng.zalo.tdtai.zalo.manager

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.Utils
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ExternalIntentManager @Inject constructor(
        private val application: ZaloApplication,
        private val resourceManager: ResourceManager,
        private val utils: Utils
) {
    fun dispatchChooserIntent(fragment: Fragment, requestCode: Int, chooserType: Int, allowMultiple: Boolean) {
        fragment.startActivityForResult(createChooserIntent(chooserType, allowMultiple), requestCode)
    }

    fun dispatchChooserIntent(activity: Activity, requestCode: Int, chooserType: Int, allowMultiple: Boolean) {
        activity.startActivityForResult(createChooserIntent(chooserType, allowMultiple), requestCode)
    }

    private fun createChooserIntent(chooserType: Int, allowMultiple: Boolean): Intent {
        val mimeType = when (chooserType) {
            CHOOSER_TYPE_IMAGE -> "image/*"
            CHOOSER_TYPE_VIDEO -> "video/*"
            CHOOSER_TYPE_AUDIO -> "audio/*"
            else -> "*/*"
        }

        val getIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        }

        val resInfo: List<ResolveInfo> = application.packageManager.queryIntentActivities(getIntent, 0)
        var found = false

        if (resInfo.isNotEmpty()) {
            for (info in resInfo) {
                if (info.activityInfo.packageName.toLowerCase() == "com.google.android.apps.photos") {
                    getIntent.setPackage(info.activityInfo.packageName)
                    found = true
                    break
                }
            }
        }

        return if (found) {
            getIntent
        } else {
            Intent.createChooser(getIntent, application.getString(R.string.label_choose_from))
        }
    }

    //return picture uri
    fun dispatchCaptureIntent(fragment: Fragment, requestCode: Int, captureType: Int, callback: ((localPath: String?) -> Unit)? = null) {
        createCaptureIntent(captureType) { captureIntent, resultRealPath ->
            fragment.startActivityForResult(captureIntent, requestCode)

            callback?.invoke(resultRealPath)
        }
    }

    fun dispatchCaptureIntent(activity: Activity, requestCode: Int, captureType: Int, callback: ((localPath: String?) -> Unit)? = null) {
        createCaptureIntent(captureType) { captureIntent, resultRealPath ->
            activity.startActivityForResult(captureIntent, requestCode)

            callback?.invoke(resultRealPath)
        }
    }

    private fun createCaptureIntent(captureType: Int, callback: (captureIntent: Intent, resultRealPath: String) -> Unit) {
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
            captureIntent.resolveActivity(application.packageManager)?.let {
                // Create the File where the photo should go
                resourceManager.createTempFile(fileType) { file ->
                    utils.assertNotNull(file, TAG, "dispatchCaptureIntent") { fileNotNull ->
                        val fileRealPath: String = fileNotNull.absolutePath
                        val fileContentUri: Uri = resourceManager.getContentUri(fileRealPath)

                        captureIntent.apply {
                            putExtra(MediaStore.EXTRA_OUTPUT, fileContentUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        }

                        application.packageManager.queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY).apply {
                            forEach { resolveInfo ->
                                val packageName = resolveInfo.activityInfo.packageName
                                application.grantUriPermission(packageName, fileContentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }

                        callback(captureIntent, fileRealPath)
                    }
                }
            }
        }
    }

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