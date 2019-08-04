package vng.zalo.tdtai.zalo.utils

import android.app.Activity
import android.content.Context
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.Task
import vng.zalo.tdtai.zalo.R
import java.io.File
import java.io.FileOutputStream
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

    fun <TResult> assertTaskSuccess(task: Task<TResult>, tag: String, logMsgPrefix: String? = null, callback: (result: TResult?) -> Unit) {
        if (task.isSuccessful) {
            callback.invoke(task.result)
        } else {
            Log.e(tag, "$logMsgPrefix: ${task::class.java.simpleName} fail")
        }
    }

    inline fun <reified T> assertNotNull(obj: T?, tag: String, logMsgPrefix: String, callback: (obj: T) -> Unit) {
        if (obj != null) {
            callback.invoke(obj)
        } else {
            Log.e(tag, "$logMsgPrefix: ${T::class.java.simpleName} is null")
        }
    }

    fun getStickerBucketNameFromName(name: String): String {
        val bucketName = StringBuilder(name.toLowerCase())
        return bucketName.replace(Regex(" "), "_")
    }

    fun dpToPx(context: Context, valueInDp: Int): Float {
        val metrics = context.resources.displayMetrics
        return valueInDp / metrics.density
    }

    fun pxToDp(context: Context, valueInPx: Float): Int {
        return (valueInPx / context.resources.displayMetrics.density).toInt()
    }

    fun parseFileName(fileUri: String): String {
        return fileUri.substring(fileUri.lastIndexOf('/') + 1)
    }

    fun cloneFileToPictureDir(context: Context, targetUri: Uri): String? {
        try {
            val mimeType = context.contentResolver.getType(targetUri)
            mimeType?.let {
                val inputStream = context.contentResolver.openInputStream(targetUri)

                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let { pictureDir ->
                    val newFileName = "${Constants.FILE_PREFIX}${Date().time}.jpg"
                    val file = File("${pictureDir.absolutePath}/$newFileName")

                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(inputStream!!.available()) // or other buffer size

                        var read = inputStream.read(buffer)
                        while (read != -1) {
                            output.write(buffer, 0, read)
                            read = inputStream.read(buffer)
                        }

                        output.flush()
                        return file.absolutePath//use this path
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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
}

// extension functions

val Any.TAG: String
    get() = this::class.java.simpleName