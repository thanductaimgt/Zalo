package vng.zalo.tdtai.zalo.abstracts

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import androidx.exifinterface.media.ExifInterface
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import java.io.File
import java.io.InputStream


object ExternalSourceManager {
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

        return if (Utils.isContentUri(localPath)) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, localFileUri)
        } else {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeFile(localPath, options)
        }
    }

    @SuppressLint("CheckResult")
    fun getVideoThumbBitmap(context: Context, videoPath: String, callback: (bitmap: Bitmap) -> Unit) {
        Single.fromCallable {
            val retriever = MediaMetadataRetriever()

            if (Utils.isContentUri(videoPath)) {
                retriever.setDataSource(context, Uri.parse(videoPath))
            } else {
                retriever.setDataSource(videoPath, HashMap())
            }
            val bitmap = retriever.frameAtTime?:retriever.getFrameAtTime(0)
            retriever.release()
            bitmap
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { bitmap:Bitmap->
                    callback(bitmap)
                }
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

    const val FILE_TYPE_IMAGE = 0
    const val FILE_TYPE_VIDEO = 1
    const val FILE_TYPE_AUDIO = 2
}