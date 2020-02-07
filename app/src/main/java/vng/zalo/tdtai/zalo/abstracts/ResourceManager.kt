package vng.zalo.tdtai.zalo.abstracts

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import vng.zalo.tdtai.zalo.SharedPrefsManager
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object ResourceManager {
    private val phoneNameMap = HashMap<String, String>()

    @SuppressLint("CheckResult")
    fun createTempFile(context: Context, fileType: Int, callback: (file: File?) -> Unit) {
        Single.fromCallable {
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
                File.createTempFile(
                        Constants.FILE_PREFIX, /* prefix */
                        ".${when (fileType) {
                            FILE_TYPE_IMAGE -> "jpg"
                            else -> "mp4"
                        }}", /* suffix */
                        it /* directory */
                ).apply { deleteOnExit() }
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { file -> callback(file) },
                        { e ->
                            Log.e(TAG, "onError: createImageFile")
                            e.printStackTrace()
                        }
                )
    }

    fun getImageDimension(context: Context, localUri: String): String {
        val inputStream = Utils.getInputStream(context, localUri)

        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inJustDecodeBounds = true

        BitmapFactory.decodeResourceStream(context.resources, TypedValue(), inputStream, Rect(), bitmapOptions)

        var width = bitmapOptions.outWidth
        var height = bitmapOptions.outHeight

        val rotationDegree = getImageRotation(context, localUri)
        if (rotationDegree == 90 || rotationDegree == 270) {
            val temp = width
            width = height
            height = temp
        }
        return "${width}:${height}"
    }

    fun getImageRotation(context: Context, localUri: String): Int {
        val input: InputStream = Utils.getInputStream(context, localUri)
        val ei: ExifInterface
        ei = if (Build.VERSION.SDK_INT > 23) ExifInterface(input) else ExifInterface(localUri)
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    fun getImageBitmap(context: Context, localPath: String): Bitmap {
        val localFileUri = Uri.parse(localPath)

        return if (isContentUri(localPath)) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, localFileUri)
        } else {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeFile(localPath, options)
        }
    }

    private fun getVideoThumbBitmap(context: Context, videoUri: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            when {
                isContentUri(videoUri) -> {
                    retriever.setDataSource(context, Uri.parse(videoUri))
                }
                isNetworkUri(videoUri) -> {
                    retriever.setDataSource(videoUri, HashMap())
                }
                else -> {
                    retriever.setDataSource(videoUri)
                }
            }
            val bitmap: Bitmap? = retriever.frameAtTime ?: retriever.getFrameAtTime(0)
            retriever.release()
            return bitmap
        } catch (t: Throwable) {
            retriever.release()
            return null
        }
    }

    private fun saveVideoThumb(context: Context, bitmap: Bitmap?): String {
        val thumbPath = "${getVideoThumbDir(context)}/${System.currentTimeMillis()}"
        val output = FileOutputStream(thumbPath)
        try {
            bitmap?.compress(Constants.IMAGE_COMPRESS_FORMAT, Constants.IMAGE_COMPRESS_QUALITY, output)
        } catch (e: Throwable) {
            File(thumbPath).delete()
            output.close()
            e.printStackTrace()
        }
        return thumbPath
    }

    @SuppressLint("CheckResult")
    fun getVideoThumbUri(context: Context, videoUri: String, callback: (uri: String) -> Unit) {
        var thumbUriString = SharedPrefsManager.getVideoThumbCacheUri(context, videoUri)
        if (thumbUriString != null && File(thumbUriString).exists()) {
            callback(thumbUriString)
        } else {
            Single.fromCallable {
                val bitmap = getVideoThumbBitmap(context, videoUri)
                thumbUriString = saveVideoThumb(context, bitmap)
                SharedPrefsManager.setVideoThumbCacheUri(context, videoUri, thumbUriString!!)
                thumbUriString!!
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { uri: String ->
                        callback(uri)
                    }
        }
    }

    private fun getVideoThumbDir(context: Context): String {
        return context.cacheDir.absolutePath
    }

    //    fun getVideoDimension(context: Context, localUri: String): String {
//        val inputStream = Utils.getInputStream(context, localUri)
//
//        val bitmapOptions = BitmapFactory.Options()
//        bitmapOptions.inJustDecodeBounds = true
//
//        BitmapFactory.decodeResourceStream(context.resources, TypedValue(), inputStream, Rect(), bitmapOptions)
//
//        return "${bitmapOptions.outWidth}:${bitmapOptions.outHeight}"
//    }
//
//    fun getMediaId()
    fun initPhoneNameMap() {
        //todo
    }

    fun getNameFromPhone(phone: String): String? {
        return phoneNameMap[phone]
    }

    fun getContentUri(context: Context, realUriString: String): Uri {
        return FileProvider.getUriForFile(
                context,
                getProviderAuthority(context),
                File(realUriString)
        )
    }

    fun getRealUriString(context: Context, contentUri: Uri): String? {
        return if (isAboveKitKat()) { // Android OS above sdk version 19.
            getUriRealPathAboveKitkat(context, contentUri)
        } else { // Android OS below sdk version 19
            getMediaRealPath(context.contentResolver, contentUri, null)
        }
    }

    private fun getUriRealPathAboveKitkat(context: Context, uri: Uri): String? {
        var res: String? = null
        if (isFileUri(uri)) {
            res = uri.path
        } else if (isDocumentUri(context, uri)) { // Get uri related document id.
            val documentId = DocumentsContract.getDocumentId(uri)
            // Get uri authority.
            val uriAuthority = uri.authority
            if (isMediaDoc(uriAuthority)) {
                val idArr = documentId.split(":").toTypedArray()
                if (idArr.size == 2) { // First item is document type.
                    val docType = idArr[0]
                    // Second item is document real id.
                    val realDocId = idArr[1]
                    // Get content uri by document type.
                    var mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    when (docType) {
                        "image" -> {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        // Get where clause with real document id.
                    }
                    // Get where clause with real document id.
                    val whereClause = MediaStore.Images.Media._ID + " = " + realDocId
                    res = getMediaRealPath(context.contentResolver, mediaContentUri, whereClause)
                }
            } else if (isDownloadDoc(uriAuthority)) { // Build download uri.
                val downloadUri = Uri.parse("content://downloads/public_downloads")
                // Append download document id at uri end.
                val downloadUriAppendId = ContentUris.withAppendedId(downloadUri, java.lang.Long.valueOf(documentId))
                res = getMediaRealPath(context.contentResolver, downloadUriAppendId, null)
            } else if (isExternalStoreDoc(uriAuthority)) {
                val idArr = documentId.split(":").toTypedArray()
                if (idArr.size == 2) {
                    val type = idArr[0]
                    val realDocId = idArr[1]
                    if ("primary".equals(type, ignoreCase = true)) {
                        res = Environment.getExternalStorageDirectory().toString() + "/" + realDocId
                    }
                }
            }
        } else if (isContentUri(uri.toString())) {
            res = if (isGooglePhotoDoc(uri.authority)) {
                uri.lastPathSegment
            } else {
                getMediaRealPath(context.contentResolver, uri, null)
            }
        }
        return res
    }

    /* Check whether current android os version is bigger than kitkat or not. */
    private fun isAboveKitKat(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    }

    /* Check whether this uri represent a document or not. */
    private fun isDocumentUri(ctx: Context?, uri: Uri?): Boolean {
        var ret = false
        if (ctx != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(ctx, uri)
        }
        return ret
    }

    /* Check whether this uri is a content uri or not.
*  content uri like content://media/external/images/media/1302716
*  */

    fun isContentUri(localPath: String): Boolean {
        return localPath.startsWith("content://")
//        return localPath.startsWith("content://")
    }

    /* Check whether this uri is a file uri or not.
*  file uri like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg
* */
    private fun isFileUri(uri: Uri): Boolean {
        var ret = false
        val uriSchema = uri.scheme
        if ("file".equals(uriSchema, ignoreCase = true)) {
            ret = true
        }
        return ret
    }

    fun isNetworkUri(uri: String): Boolean {
        return uri.startsWith("http")
    }


    /* Check whether this document is provided by ExternalStorageProvider. */
    private fun isExternalStoreDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.externalstorage.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Check whether this document is provided by DownloadsProvider. */
    private fun isDownloadDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.providers.downloads.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Check whether this document is provided by MediaProvider. */
    private fun isMediaDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.providers.media.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Check whether this document is provided by google photos. */
    private fun isGooglePhotoDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.google.android.apps.photos.content" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Return uri represented document file real local path.*/
    private fun getMediaRealPath(contentResolver: ContentResolver, uri: Uri, whereClause: String?): String? {
        var res: String? = null
        // Query the uri with condition.
        with(contentResolver.query(uri, null, whereClause, null, null)) {
            if (this != null && moveToFirst()) { // Get columns name by uri type.
                var columnName = MediaStore.Images.Media.DATA
                when (uri) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI -> {
                        columnName = MediaStore.Images.Media.DATA
                    }
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI -> {
                        columnName = MediaStore.Audio.Media.DATA
                    }
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI -> {
                        columnName = MediaStore.Video.Media.DATA
                    }
                    // Get column index.
                    // Get column value which is the uri related file local path.
                }
                // Get column index.
                val imageColumnIndex: Int = getColumnIndex(columnName)
                // Get column value which is the uri related file local path.
                res = getString(imageColumnIndex)
            }
        }
        return res
    }

    private fun getProviderAuthority(context: Context): String {
        return "${context.applicationContext.packageName}.${Constants.PROVIDER_AUTHORITY}"
    }

    const val FILE_TYPE_IMAGE = 0
    const val FILE_TYPE_VIDEO = 1
    const val FILE_TYPE_AUDIO = 2
}