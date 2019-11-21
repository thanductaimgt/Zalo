package vng.zalo.tdtai.zalo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Task
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import vng.zalo.tdtai.zalo.R
import java.io.File
import java.io.InputStream
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
            else -> SimpleDateFormat.getDateInstance().format(date)
        }
    }

    private fun formatTime(num: Long, unit: String): String {
        return "$num $unit"
    }

    fun getTimeDiffInMillis(start: Date, end: Date): Long {
        return end.time - start.time
    }

    fun areInDifferentDay(date1: Date, date2: Date): Boolean {
        val formatDate1 = SimpleDateFormat.getDateInstance().format(date1)
        val formatDate2 = SimpleDateFormat.getDateInstance().format(date2)
        return formatDate1 != formatDate2
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

    private fun parseFileName(fileUri: String): String {
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

    fun dispatchChooserIntent(activity: Activity, requestCode: Int) {
        val mimeType = when (requestCode) {
            Constants.CHOOSE_IMAGES_REQUEST -> "image/*"
            else -> "*/*"
        }

        val getIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply { type = mimeType }

        val chooserIntent = Intent.createChooser(getIntent, activity.getString(R.string.label_choose_image_from)).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        }

        activity.startActivityForResult(chooserIntent, requestCode)
    }

    //return picture uri
    fun dispatchTakePictureIntent(activity: Activity, callback: ((localPath: String?) -> Unit)? = null) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity.packageManager)?.let {
                // Create the File where the photo should go
                createImageFile(activity) { file ->
                    if (assertNotNull(file, TAG, "createImageFile")) {
                        val photoUri: Uri = FileProvider.getUriForFile(
                                activity,
                                getProviderAuthority(activity),
                                file!!
                        )
                        takePictureIntent.apply {
                            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        activity.packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY).apply {
                            forEach { resolveInfo ->
                                val packageName = resolveInfo.activityInfo.packageName
                                activity.grantUriPermission(packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }

                        activity.startActivityForResult(takePictureIntent, Constants.TAKE_PICTURE_REQUEST)

                        callback?.invoke(file.path)
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun createImageFile(context: Context, callback: (file: File?) -> Unit) {
        Single.fromCallable {
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
                File.createTempFile(
                        "${Constants.FILE_PREFIX}${Date().time}", /* prefix */
                        ".jpg", /* suffix */
                        it /* directory */
                )
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

    fun deleteZaloFileAtUri(context: Context, uriString: String?): Boolean {
        var res = true
        uriString?.let {
            if (parseFileName(uriString).startsWith(Constants.FILE_PREFIX)) {
                val file = File(uriString)
                res = file.delete() || file.canonicalFile.delete() || context.applicationContext.deleteFile(
                        file.name
                )
            }
        }
        return res
    }

    fun getImageDimension(context: Context, localPath: String): String {
        val inputStream = getInputStream(context, localPath)

        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inJustDecodeBounds = true

        BitmapFactory.decodeResourceStream(context.resources, TypedValue(), inputStream, Rect(), bitmapOptions)

        return "${bitmapOptions.outWidth}:${bitmapOptions.outHeight}"
    }

    fun getInputStream(context: Context, localPath: String): InputStream {
        return if (isContentUri(localPath)) {
            context.contentResolver.openInputStream(Uri.parse(localPath))!!
        } else {
            File(localPath).inputStream()
        }
    }

    private fun isContentUri(localPath: String): Boolean {
        return localPath.startsWith("content://")
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

    fun getFileExtensionFromFileName(fileName: String): String {
        var extension = fileName.takeLastWhile { it != '.' }
        if (extension.length == fileName.length)
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
    fun getCallTimeFormat(context: Context, callTime:Int):String{
        var time = callTime.toLong()
        val res = StringBuilder()

        if(time > 3600) {
            val hours = time/3600
            res.append("${formatTime(hours, context.getString(R.string.label_hour))} ")
            time -= hours * 3600
        }

        val mins = time/60
        res.append("${formatTime(mins, context.getString(R.string.label_minute))} ")
        time -= mins * 60

        val secs = time
        res.append(formatTime(secs, context.getString(R.string.label_second)))
        return res.toString()
    }
}

// extension functions

val Any.TAG: String
    get() = this::class.java.simpleName