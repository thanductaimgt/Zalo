package vng.zalo.tdtai.zalo.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.camera.core.ImageProxy
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.squareup.picasso.*
import com.squareup.picasso.Target
import dagger.Lazy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.base.OnDoubleClickListener
import vng.zalo.tdtai.zalo.base.OnHoldListener
import vng.zalo.tdtai.zalo.manager.ResourceManager
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

@Singleton
class Utils @Inject constructor(
        private val application: ZaloApplication,
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

    fun getTimeDiffFormat(milli: Long): String {
        val curMilli = System.currentTimeMillis()
        val diffByMillisecond = curMilli - milli
        return when {
            diffByMillisecond < Constants.ONE_MIN_IN_MILLISECOND -> application.getString(R.string.label_just_now)
            diffByMillisecond < Constants.ONE_HOUR_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_MIN_IN_MILLISECOND.toLong(), application.getString(R.string.label_minute))
            diffByMillisecond < Constants.ONE_DAY_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_HOUR_IN_MILLISECOND.toLong(), application.getString(R.string.label_hour))
            diffByMillisecond < Constants.SEVEN_DAYS_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_DAY_IN_MILLISECOND.toLong(), application.getString(R.string.label_day))
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
            task.exception?.printStackTrace()
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

    fun dpToPx(dp: Number): Int {
        return (dp.toFloat() * (application.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT.toFloat())).toInt()
    }

    fun pxToDp(px: Number): Int {
        return (px.toFloat() / (application.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT.toFloat())).toInt()
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view.windowToken != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showKeyboard(view: View) {
        view.post {
            view.requestFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun getInputStream(localPath: String): InputStream {
        return if (lazyResourceManager.get().isContentUri(localPath)) {
            application.contentResolver.openInputStream(getUri(localPath))!!
        } else {
            File(localPath).inputStream()
        }
    }

    fun getPhoneFromSipProfileName(sipProfileName: String): String {
        return sipProfileName.takeLastWhile { it != '.' }
    }

    fun getResIdFromFileExtension(fileExtension: String): Int {
        val resourceId = application.resources.getIdentifier(
                fileExtension.toLowerCase(Locale.US), "drawable",
                application.packageName
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
    fun getSize(ratio: String): Size {
        return ratio.split(":").let { Size(it[0].toInt(), it[1].toInt()) }
    }

    //callTime: seconds
    fun getCallTimeFormat(callTime: Int): String {
        var time = callTime.toLong()
        val res = StringBuilder()

        if (time > 3600) {
            val hours = time / 3600
            res.append("${formatTime(hours, application.getString(R.string.label_hour))} ")
            time -= hours * 3600
        }

        val mins = time / 60
        res.append("${formatTime(mins, application.getString(R.string.label_minute))} ")
        time -= mins * 60

        val secs = time
        res.append(formatTime(secs, application.getString(R.string.label_second)))
        return res.toString()
    }

    fun getLastOnlineTimeFormat(lastOnlineTime: Long?): String {
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
                application.getString(R.string.description_just_online)
            }
            else -> {
                "${application.getString(R.string.description_last_seen)}: ${
                if (diffByMillisecond >= Constants.SEVEN_DAYS_IN_MILLISECOND) {
                    getDateFormat(lastOnlineTimeMilli)
                } else {
                    "${
                    when {
                        diffByMillisecond < Constants.ONE_MIN_IN_MILLISECOND -> application.getString(R.string.description_less_than_one_min)
                        diffByMillisecond < Constants.ONE_HOUR_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_MIN_IN_MILLISECOND.toLong(), application.getString(R.string.label_minute))
                        diffByMillisecond < Constants.ONE_DAY_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_HOUR_IN_MILLISECOND.toLong(), application.getString(R.string.label_hour))
                        else -> formatTime(diffByMillisecond / Constants.ONE_DAY_IN_MILLISECOND.toLong(), application.getString(R.string.label_day))
                    }
                    } ${application.getString(R.string.description_ago)}"
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

    fun getMetricFormat(metric: Int): String {
        val locale = Locale.US
        return when {
            metric < 1000 -> metric.toString()
            metric < 1000000 -> String.format(locale, "%.1fK", metric / 1000f)
            metric < 1000000000 -> String.format(locale, "%.1fM", metric / 1000000f)
            else -> String.format(locale, "%.1fB", metric / 1000000000f)
        }
    }

    fun rotateImage(img: Bitmap, degree: Int, doFlip: Boolean = false, targetRatio: Float? = null): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        if (doFlip) {
            matrix.preScale(1f, -1f)
        }
        var startX = 0
        var startY = 0
        var width = img.width
        var height = img.height
        if (targetRatio != null) {
            @Suppress("NAME_SHADOWING")
            val targetRatio = if (degree % 180 != 0) {
                1 / targetRatio
            } else {
                targetRatio
            }

            val curRatio = width / height.toFloat()
            if (curRatio > targetRatio) {
                width = (height * targetRatio).toInt()
                startX = (img.width - width) / 2
            } else {
                height = (width / targetRatio).toInt()
                startY = (img.height - height) / 2
            }
        }
        return Bitmap.createBitmap(img, startX, startY, width, height, matrix, true)
    }

    fun getAdjustedRatio(ratio: String): String {
        val size = getSize(ratio)
        val floatRatio = size.width / size.height.toFloat()
        val adjustedRatio = min(max(floatRatio, MIN_RATIO), MAX_RATIO)
        val newHeight: Int
        val newWidth: Int
        if (size.width > size.height) {
            newHeight = size.height
            newWidth = (newHeight * adjustedRatio).toInt()
        } else {
            newWidth = size.width
            newHeight = (newWidth / adjustedRatio).toInt()
        }
        return "$newWidth:$newHeight"
    }

    companion object {
        const val MIN_RATIO = 0.8f
        const val MAX_RATIO = 2f
    }
}

// extension functions

val <T> T?.TAG: String
    get() {
        return T.javaClass.let { it.enclosingClass?.name ?: it.name }
//        return T::class.java.simpleName
    }

private fun Picasso.loadCompat(url: String?, resourceManager: ResourceManager): RequestCreator {
    val doCache: Boolean
    val urlToLoad: String?
    if (url == null || resourceManager.isNetworkUri(url) || resourceManager.isContentUri(url)) {
        urlToLoad = url
        doCache = url != null && resourceManager.isNetworkUri(url)
    } else {
        urlToLoad = "file://$url"
        doCache = false
    }
    return load(urlToLoad).let { if (!doCache) it.networkPolicy(NetworkPolicy.NO_STORE) else it }
}

fun Picasso.smartLoad(url: String?, resourceManager: ResourceManager, imageView: ImageView, applyConfig: ((requestCreator: RequestCreator) -> Unit)? = null) {
    var requestCreator = loadCompat(url, resourceManager)
    applyConfig?.invoke(requestCreator)

    requestCreator.networkPolicy(NetworkPolicy.OFFLINE)
            .into(imageView, object : Callback.EmptyCallback() {
                override fun onError(e: Exception?) {
                    requestCreator = Picasso.get().loadCompat(url, resourceManager)
                    applyConfig?.invoke(requestCreator)

                    requestCreator.networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(imageView)
                }
            })
}

fun Picasso.smartLoad(url: String?, resourceManager: ResourceManager, target: Target, applyConfig: ((requestCreator: RequestCreator) -> Unit)? = null) {
    var requestCreator = loadCompat(url, resourceManager)
    applyConfig?.invoke(requestCreator)

    requestCreator.networkPolicy(NetworkPolicy.OFFLINE)
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    target.onPrepareLoad(placeHolderDrawable)
                }

                override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                    requestCreator = Picasso.get().loadCompat(url, resourceManager)
                    applyConfig?.invoke(requestCreator)

                    requestCreator.networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(object : Target {
                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                                override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                                    target.onBitmapFailed(e, errorDrawable)
                                }

                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                    target.onBitmapLoaded(bitmap, from)
                                }
                            })
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    target.onBitmapLoaded(bitmap, from)
                }
            })
}

//fun Image.toBitmap(): Bitmap {
//    val yBuffer = planes[0].buffer // Y
//    val uBuffer = planes[1].buffer // U
//    val vBuffer = planes[2].buffer // V
//
//    val ySize = yBuffer.remaining()
//    val uSize = uBuffer.remaining()
//    val vSize = vBuffer.remaining()
//
//    val nv21 = ByteArray(ySize + uSize + vSize)
//
//    //U and V are swapped
//    yBuffer.get(nv21, 0, ySize)
//    vBuffer.get(nv21, ySize, vSize)
//    uBuffer.get(nv21, ySize + vSize, uSize)
//
//    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
//    val out = ByteArrayOutputStream()
//    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
//    val imageBytes = out.toByteArray()
//    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//}

@SuppressLint("UnsafeExperimentalUsageError")
fun ImageProxy.toBitmap(): Bitmap {
    val buffer: ByteBuffer = image!!.planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}

fun ViewPager2.animatePagerTransition(forward: Boolean) {
    val animator: ValueAnimator = ValueAnimator.ofInt(0, width - if (forward) paddingLeft else paddingRight)
    animator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {
            endFakeDrag()
        }

        override fun onAnimationCancel(animation: Animator?) {
            endFakeDrag()
        }

        override fun onAnimationRepeat(animation: Animator?) {}
    })
    animator.interpolator = LinearInterpolator()
    animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
        private var oldDragPosition = 0
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val dragPosition = animation.animatedValue as Int
            val dragOffset = dragPosition - oldDragPosition
            oldDragPosition = dragPosition
            fakeDragBy((dragOffset * if (forward) -1 else 1).toFloat())
        }
    })
    animator.duration = 300
    beginFakeDrag()
    animator.start()
}

private const val MAX_WHERE_IN = 10
fun Query.whereInSafe(fieldPath: FieldPath, values: List<Any>): Query {
    return whereIn(fieldPath, values.take(MAX_WHERE_IN))
}

fun Query.whereInSafe(field: String, values: List<Any>): Query {
    return whereIn(field, values.take(MAX_WHERE_IN))
}

fun View.setOnHoldListener(callback: (isHold: Boolean) -> Unit) {
    setOnTouchListener(object : OnHoldListener() {
        override fun onHoldStateChange(isHold: Boolean) {
            callback(isHold)
        }
    })
}

fun View.setOnDoubleClickListener(callback: () -> Unit) {
    setOnTouchListener(object : OnDoubleClickListener() {
        override fun onDoubleClick() {
            callback()
        }
    })
}

fun Disposable.addTo(compositeDisposable: CompositeDisposable?) {
    compositeDisposable?.add(this)
}