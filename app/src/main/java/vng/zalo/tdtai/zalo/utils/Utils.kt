package vng.zalo.tdtai.zalo.utils

import android.content.Context
import android.content.Intent
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import dagger.Lazy
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.managers.ResourceManager
import java.io.File
import java.io.InputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap

@Singleton
class Utils @Inject constructor(
        private val lazyResourceManager: Lazy<ResourceManager>
) {
    private val uriCache = HashMap<String, Uri>()

    fun getUri(uriString: String): Uri {
        var res = uriCache[uriString]
        if (res == null) {
            res = Uri.parse(uriString)
            uriCache[uriString] = res
        }
        return res!!
    }

    fun getTimeDiffFormat(context: Context, milli: Long): String {
        val curMilli = System.currentTimeMillis()
        val diffByMillisecond = curMilli - milli
        return when {
            diffByMillisecond < Constants.ONE_MIN_IN_MILLISECOND -> context.getString(R.string.label_just_now)
            diffByMillisecond < Constants.ONE_HOUR_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_MIN_IN_MILLISECOND.toLong(), context.getString(R.string.label_minute))
            diffByMillisecond < Constants.ONE_DAY_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_HOUR_IN_MILLISECOND.toLong(), context.getString(R.string.label_hour))
            diffByMillisecond < Constants.SEVEN_DAYS_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_DAY_IN_MILLISECOND.toLong(), context.getString(R.string.label_day))
            else -> getDateFormat(milli)
        }
    }

    private fun formatTime(num: Long, unit: String): String {
        return "$num $unit"
    }

    fun areInDifferentDay(milli1: Long, milli2: Long): Boolean {
        val day1 = milli1 / Constants.ONE_DAY_IN_MILLISECOND
        val day2 = milli2 / Constants.ONE_DAY_IN_MILLISECOND
        return day1 != day2
    }

    fun areInDifferentMin(milli1: Long, milli2: Long): Boolean {
        val min1 = milli1 / Constants.ONE_MIN_IN_MILLISECOND
        val min2 = milli2 / Constants.ONE_MIN_IN_MILLISECOND
        return min1 != min2
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

    fun dpToPx(context: Context, valueInDp: Int): Float {
        val metrics = context.resources.displayMetrics
        return valueInDp / metrics.density
    }

    fun pxToDp(context: Context, valueInPx: Float): Int {
        return (valueInPx / context.resources.displayMetrics.density).toInt()
    }

//    fun parseFileName(fileUri: String): String {
//        return fileUri.substring(fileUri.lastIndexOf('/') + 1)
//    }

    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    fun showKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun getInputStream(context: Context, localPath: String): InputStream {
        return if (lazyResourceManager.get().isContentUri(localPath)) {
            context.contentResolver.openInputStream(getUri(localPath))!!
        } else {
            File(localPath).inputStream()
        }
    }

    fun getPhoneFromSipProfileName(sipProfileName: String): String {
        return sipProfileName.takeLastWhile { it != '.' }
    }

    fun getResIdFromFileExtension(context: Context, fileExtension: String): Int {
        val resourceId = context.resources.getIdentifier(
                fileExtension.toLowerCase(Locale.US), "drawable",
                context.packageName
        )

        return if (resourceId != 0) resourceId else R.drawable.file
    }

//    fun getFileNameFromLocalUri(uri: String): String {
//        val filePathWithoutSeparator =
//                if (uri.endsWith(File.separator)) uri.dropLast(1) else uri
//        return filePathWithoutSeparator
//                .takeLastWhile { it != '/' }
//    }

    fun getFileExtension(filePath: String?): String {
        return filePath?.let {
            val extension = filePath.takeLastWhile { it != '.' }
            if (extension.length == filePath.length)
                ""
            else
                extension
        } ?: ""
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

    fun getLastOnlineTimeFormat(context: Context, lastOnlineTime: Long?): String {
        val curMilli = System.currentTimeMillis()
        val lastOnlineTimeMilli: Long

        val diffByMillisecond: Long

        if (lastOnlineTime == null) {
            lastOnlineTimeMilli = curMilli
            diffByMillisecond = 0
        } else {
            lastOnlineTimeMilli = lastOnlineTime
            diffByMillisecond = curMilli - lastOnlineTimeMilli
        }

        return when (diffByMillisecond) {
            0L -> {
                context.getString(R.string.description_just_online)
            }
            else -> {
                "${context.getString(R.string.description_last_seen)}: ${
                if (diffByMillisecond >= Constants.SEVEN_DAYS_IN_MILLISECOND) {
                    getDateFormat(lastOnlineTimeMilli)
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

    fun setFullscreen(window: Window, isFull: Boolean) {
        val attrs = window.attributes
        attrs.flags = if (isFull) {
            attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
        } else {
            attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        }
        window.attributes = attrs
    }

    fun getTimeFormat(milli: Long): String {
        return SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(milli)
    }

    fun getDateFormat(milli: Long): String {
        return SimpleDateFormat.getDateInstance().format(milli)
    }

    fun <T> getTwoListDiffElement(list1: List<T>, list2: List<T>): List<T> {
        val diff1 = list1.toMutableList().apply { removeAll(list2) }
        val diff2 = list2.toMutableList().apply { removeAll(list1) }
        return diff1.apply { addAll(diff2) }
    }

    fun getLocalUris(intent: Intent): List<String> {
        val localUris = ArrayList<String>()
        if (null != intent.clipData) {
            for (i in 0 until intent.clipData!!.itemCount) {
                val uri = intent.clipData!!.getItemAt(i).uri.toString()
                localUris.add(uri)
            }
        } else {
            val uri = intent.data!!.toString()
            localUris.add(uri)
        }
        return localUris.reversed()
    }
}

// extension functions

val Any.TAG: String
    get() = this::class.java.simpleName

fun Picasso.loadCompat(url: String?, resourceManager: ResourceManager): RequestCreator {
    return load(if (url == null || resourceManager.isNetworkUri(url) || resourceManager.isContentUri(url)) {
        url
    } else {
        "file://$url"
    })
}