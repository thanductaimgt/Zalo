package vng.zalo.tdtai.zalo.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.R
import java.io.File
import java.io.InputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


object Utils {
    fun getTimeDiffFormat(context: Context, date: Date): String {
        val curDate = Date()
        val diffByMillisecond = getTimeDiffInMillis(date, curDate)
        return when {
            diffByMillisecond < Constants.ONE_MIN_IN_MILLISECOND -> context.getString(R.string.label_just_now)
            diffByMillisecond < Constants.ONE_HOUR_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_MIN_IN_MILLISECOND.toLong(), context.getString(R.string.label_minute))
            diffByMillisecond < Constants.ONE_DAY_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_HOUR_IN_MILLISECOND.toLong(), context.getString(R.string.label_hour))
            diffByMillisecond < Constants.SEVEN_DAYS_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_DAY_IN_MILLISECOND.toLong(), context.getString(R.string.label_day))
            else -> getDateFormat(date)
        }
    }

    private fun formatTime(num: Long, unit: String): String {
        return "$num $unit"
    }

    fun getTimeDiffInMillis(start: Date, end: Date): Long {
        return end.time - start.time
    }

    fun areInDifferentDay(date1: Date, date2: Date): Boolean {
        val format1 = getDateFormat(date1)
        val format2 = getDateFormat(date2)
        return format1 != format2
    }

    fun areInDifferentMin(date1: Date, date2: Date): Boolean {
        val format1 = getTimeFormat(date1)
        val format2 = getTimeFormat(date2)
        return format1 != format2
    }

    fun <TResult> assertTaskSuccessAndResultNotNull(task: Task<TResult>, tag: String, logMsgPrefix: String, callback: (result: TResult) -> Unit) {
        assertTaskSuccess(task, tag, logMsgPrefix) {
            if (task.result != null) {
                callback.invoke(task.result!!)
            } else {
                Log.e(tag, "$logMsgPrefix: task.result is null")
            }
        }
    }

    fun <TResult> assertTaskSuccess(task: Task<TResult>, tag: String, logMsgPrefix: String? = null, callback: ((result: TResult?) -> Unit)? = null) {
        if (task.isSuccessful) {
            callback?.invoke(task.result)
            Log.d(tag, "$logMsgPrefix: ${task::class.java.simpleName} success")
        } else {
            Log.e(tag, "$logMsgPrefix: ${task::class.java.simpleName} fail")
        }
    }

    inline fun <reified T> assertNotNull(obj: T?, tag: String, logMsgPrefix: String, noinline callback: ((obj: T) -> Unit)? = null): Boolean {
        return if (obj != null) {
            callback?.invoke(obj)
            true
        } else {
            Log.e(tag, "$logMsgPrefix: ${T::class.java.simpleName} is null")
            false
        }
    }

    fun getStickerBucketNameFromName(name: String): String {
        val bucketName = StringBuilder(name.toLowerCase(Locale.US))
        return bucketName.replace(Regex(" "), "_")
    }

//    fun dpToPx(context: Context, valueInDp: Int): Float {
//        val metrics = context.resources.displayMetrics
//        return valueInDp / metrics.density
//    }
//
//    fun pxToDp(context: Context, valueInPx: Float): Int {
//        return (valueInPx / context.resources.displayMetrics.density).toInt()
//    }

    fun parseFileName(fileUri: String): String {
        return fileUri.substring(fileUri.lastIndexOf('/') + 1)
    }

    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    fun showKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun getProviderAuthority(context: Context): String {
        return "${context.applicationContext.packageName}.${Constants.PROVIDER_AUTHORITY}"
    }

    fun getInputStream(context: Context, localPath: String): InputStream {
        return if (isContentUri(localPath)) {
            context.contentResolver.openInputStream(Uri.parse(localPath))!!
        } else {
            File(localPath).inputStream()
        }
    }

    fun isContentUri(localPath: String): Boolean {
        return isContentUri(Uri.parse(localPath))
//        return localPath.startsWith("content://")
    }

    fun getPhoneFromSipProfileName(sipProfileName: String): String {
        return sipProfileName.takeLastWhile { it != '.' }
    }

    fun getResIdFromFileExtension(context: Context, fileExtension: String): Int {
        val resourceId = context.resources.getIdentifier(
                fileExtension, "drawable",
                context.packageName
        )

        return if (resourceId != 0) resourceId else R.drawable.file
    }

    fun getFileNameFromLocalUri(uri: String): String {
        val filePathWithoutSeparator =
                if (uri.endsWith(File.separator)) uri.dropLast(1) else uri
        return filePathWithoutSeparator
                .takeLastWhile { it != '/' }
    }

    fun getFileExtension(filePath: String): String {
        var extension = filePath.takeLastWhile { it != '.' }
        if (extension.length == filePath.length)
            extension = ""
        return extension
    }

    fun getFormatFileSize(bytes: Long): String {
        return when {
            bytes < Constants.ONE_KB_IN_B -> "$bytes B"
            bytes < Constants.ONE_MB_IN_B -> String.format(
                    "%.1f KB",
                    bytes / Constants.ONE_KB_IN_B.toFloat()
            )
            bytes < Constants.ONE_GB_IN_B -> String.format(
                    "%.2f MB",
                    bytes / Constants.ONE_MB_IN_B.toFloat()
            )
            else -> String.format("%.3f GB", bytes / Constants.ONE_GB_IN_B.toFloat())
        }
    }

    //width , height
    fun getDimension(ratio: String): Pair<Int, Int> {
        return ratio.split(":").let { Pair(it[0].toInt(), it[1].toInt()) }
    }

    //callTime: seconds
    fun getCallTimeFormat(context: Context, callTime: Int): String {
        var time = callTime.toLong()
        val res = StringBuilder()

        if (time > 3600) {
            val hours = time / 3600
            res.append("${formatTime(hours, context.getString(R.string.label_hour))} ")
            time -= hours * 3600
        }

        val mins = time / 60
        res.append("${formatTime(mins, context.getString(R.string.label_minute))} ")
        time -= mins * 60

        val secs = time
        res.append(formatTime(secs, context.getString(R.string.label_second)))
        return res.toString()
    }

    fun getLastOnlineTimeFormat(context: Context, lastOnlineTime: Timestamp?): String {
        val curDate = Date()
        val date: Date

        val diffByMillisecond: Long

        if (lastOnlineTime == null) {
            date = Date()
            diffByMillisecond = 0
        } else {
            date = lastOnlineTime.toDate()
            diffByMillisecond = getTimeDiffInMillis(date, curDate)
        }

        return when (diffByMillisecond) {
            0L -> {
                context.getString(R.string.description_just_online)
            }
            else -> {
                "${context.getString(R.string.description_last_seen)}: ${
                if (diffByMillisecond >= Constants.SEVEN_DAYS_IN_MILLISECOND) {
                    getDateFormat(date)
                } else {
                    "${
                    when {
                        diffByMillisecond < Constants.ONE_MIN_IN_MILLISECOND -> context.getString(R.string.description_less_than_one_min)
                        diffByMillisecond < Constants.ONE_HOUR_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_MIN_IN_MILLISECOND.toLong(), context.getString(R.string.label_minute))
                        diffByMillisecond < Constants.ONE_DAY_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_HOUR_IN_MILLISECOND.toLong(), context.getString(R.string.label_hour))
                        else -> formatTime(diffByMillisecond / Constants.ONE_DAY_IN_MILLISECOND.toLong(), context.getString(R.string.label_day))
                    }
                    } ${context.getString(R.string.description_ago)}"
                }
                }"
            }
        }
    }

    //callTime: seconds
    fun getVideoDurationFormat(duration: Int): String {
        var time = duration.toLong()
        val res = StringBuilder()

        if (time > 3600) {
            val hours = time / 3600
            res.append("${if (hours < 10) "0" else ""}$hours:")
            time -= hours * 3600
        }

        val mins = time / 60
        res.append("${if (mins < 10) "0" else ""}$mins:")
        time -= mins * 60

        val secs = time
        res.append("${if (secs < 10) "0" else ""}$secs")
        return res.toString()
    }

    fun getNotificationIdFromRoom(roomId: String): Int {
        return roomId.hashCode().shl(Constants.NOTIFICATION_PENDING_INTENT)
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
        } else if (isContentUri(uri)) {
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
    private fun isContentUri(uri: Uri): Boolean {
        var ret = false
        val uriSchema = uri.scheme
        if ("content".equals(uriSchema, ignoreCase = true)) {
            ret = true
        }
        return ret
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

    fun setFullscreen(window: Window, isFull:Boolean){
        val attrs = window.attributes
        attrs.flags = if(isFull){
            attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
        }else{
            attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        }
        window.attributes = attrs
    }

    fun getTimeFormat(date: Date):String{
        return SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(date)
    }

    fun getDateFormat(date: Date):String{
        return SimpleDateFormat.getDateInstance().format(date)
    }

    fun isNetworkUri(uri:String):Boolean{
        return uri.startsWith("http")
    }
}

// extension functions

val Any.TAG: String
    get() = this::class.java.simpleName