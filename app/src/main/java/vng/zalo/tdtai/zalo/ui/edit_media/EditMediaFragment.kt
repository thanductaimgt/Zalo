package vng.zalo.tdtai.zalo.ui.edit_media

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_create_story.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.smartLoad
import vng.zalo.tdtai.zalo.widget.FaceView
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.min


class EditMediaFragment() : BaseFragment() {
    private var bitmap: Bitmap? = null
    private var videoUrl: String? = null

    constructor(bitmap: Bitmap) : this() {
        this.bitmap = bitmap
    }

    constructor(videoUri: String) : this() {
        this.videoUrl = videoUri
    }

    lateinit var activity: BaseActivity

    private val viewModel: EditMediaViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var mediaSourceFactory: MediaSourceFactory

    private var isMuted = false
    private var isPreparing = false
    private var currentVolume = 0f

    private lateinit var detector: FirebaseVisionFaceDetector

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity = requireActivity() as BaseActivity
        return inflater.inflate(R.layout.fragment_create_story, container, false)
    }

    override fun onBindViews() {
        Picasso.get().smartLoad(sessionManager.curUser!!.avatarUrl, resourceManager, avatarImgView) {
            it.fit().centerCrop().error(R.drawable.default_peer_avatar)
        }

        if (bitmap != null) {
            initFaceDetector()
        } else {
            isPreparing = true

            val mediaSource = LoopingMediaSource(
                    mediaSourceFactory.createMediaSource(utils.getUri(videoUrl!!))
            )
            exoPlayer.prepare(mediaSource)
            exoPlayer.playWhenReady = true
            currentVolume = exoPlayer.volume

            playerView.player = exoPlayer

            muteImgView.visibility = View.VISIBLE
            muteImgView.setOnClickListener(this)
        }

        closeImgView.setOnClickListener(this)
        downloadImgView.setOnClickListener(this)
        emojiImgView.setOnClickListener(this)
        addTextImgView.setOnClickListener(this)
        sendImgView.setOnClickListener(this)
        avatarImgView.setOnClickListener(this)
        nameTextView.setOnClickListener(this)
    }

    private fun initFaceDetector() {
        val highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
//                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

        // Real-time contour detection of multiple faces
        val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .setMinFaceSize(0.2f)
                .build()

        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(realTimeOpts)

        detectFace()
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun detectFace() {
        imageView.setImageBitmap(bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap!!.config == Bitmap.Config.HARDWARE) {
            bitmap!!.copy(Bitmap.Config.ARGB_8888, true)?.let { bitmap ->
                detectFacesInternal(bitmap)
            } ?: Log.d(TAG, "copy bitmap fail")
        } else {
            detectFacesInternal(bitmap!!)
        }
    }

    private fun detectFacesInternal(bitmap: Bitmap) {
        val byteArray = toNv21(bitmap.width, bitmap.height, bitmap)
        val metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(ImageFormat.NV21)
                .setWidth(bitmap.width)
                .setHeight(bitmap.height)
                .build()

        val image = FirebaseVisionImage.fromByteArray(byteArray, metadata)

        detector.detectInImage(image)
                .addOnSuccessListener { faces ->
                    Log.d(TAG, "detect success")
                    attachFaces(bitmap, faces)
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show()
                    it.printStackTrace()
                }
    }

    private fun toNv21(inputWidth: Int, inputHeight: Int, scaled: Bitmap): ByteArray {

        val argb = IntArray(inputWidth * inputHeight)

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputHeight * inputWidth + 2 * ceil(inputHeight / 2.0).toInt() * ceil(inputWidth / 2.0).toInt())
        //val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight)

//        scaled.recycle()

        return yuv
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                a = argb[index] and -0x1000000 shr 24 // a is not used obviously
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0
                // well known RGB to YUV algorithm
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                    yuv420sp[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                }
                index++
            }
        }
    }

    private fun attachFaces(bitmap: Bitmap, faces: List<FirebaseVisionFace>) {
        imageView?.post {
            val viewWidth = imageView.width
            val viewHeight = imageView.height
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            val scaleX = viewWidth / bitmapWidth.toFloat()
            val scaleY = viewHeight / bitmapHeight.toFloat()
            val scale = min(scaleX, scaleY)

            val bitmapWidthScaled = bitmapWidth * scale
            val bitmapHeightScaled = bitmapHeight * scale

            val cropX = (viewWidth - bitmapWidthScaled) / 2
            val cropY = (viewHeight - bitmapHeightScaled) / 2

            rootLayout.clipChildren = false
            val set = ConstraintSet()
            set.clone(rootLayout)

            var biggestFaceView: FaceView? = null
            var biggestFaceViewArea: Float? = null

            for (face in faces) {
                val faceView = getFaceView()
                faceView.id = View.generateViewId()
                rootLayout.addView(faceView)

                val rect = face.boundingBox

                var marginStart = (rect.left * scale + cropX).toInt()
                var marginEnd = (bitmapWidthScaled - rect.right * scale + cropX).toInt()
                var marginTop = (rect.top * scale + cropY).toInt()
                var marginBottom = (bitmapHeightScaled - rect.bottom * scale + cropY).toInt()

                val centerX = rect.centerX()
                val centerY = rect.centerY()

                val halfBitmapWidth = bitmapWidth / 2
                val halfBitmapHeight = bitmapHeight / 2

                val halfCloseButtonSize = faceView.closeButtonSize / 2

                faceView.closeButtonPosition = when {
                    centerX < halfBitmapWidth && centerY >= halfBitmapHeight -> {
                        marginTop -= halfCloseButtonSize
                        marginEnd -= halfCloseButtonSize

                        FaceView.POSITION_TOP_END
                    }
                    centerX < halfBitmapWidth && centerY < halfBitmapHeight -> {
                        marginBottom -= halfCloseButtonSize
                        marginEnd -= halfCloseButtonSize

                        FaceView.POSITION_BOTTOM_END
                    }
                    centerX >= halfBitmapWidth && centerY < halfBitmapHeight -> {
                        marginBottom -= halfCloseButtonSize
                        marginStart -= halfCloseButtonSize

                        FaceView.POSITION_BOTTOM_START
                    }
                    else -> {
                        marginTop -= halfCloseButtonSize
                        marginStart -= halfCloseButtonSize

                        FaceView.POSITION_TOP_START
                    }
                }

                set.connect(faceView.id, ConstraintSet.TOP, rootLayout.id, ConstraintSet.TOP, marginTop)
                set.connect(faceView.id, ConstraintSet.START, rootLayout.id, ConstraintSet.START, marginStart)
                set.connect(faceView.id, ConstraintSet.BOTTOM, rootLayout.id, ConstraintSet.BOTTOM, marginBottom)
                set.connect(faceView.id, ConstraintSet.END, rootLayout.id, ConstraintSet.END, marginEnd)

                faceView.setOnCloseListener { rootLayout.removeView(faceView) }
                faceView.setOnFaceClickListener { Toast.makeText(requireContext(), "face clicked", Toast.LENGTH_SHORT).show() }

                val curArea = (rect.right - rect.left) * (rect.bottom - rect.top) * scale * scale

                if (biggestFaceViewArea == null || biggestFaceViewArea < curArea) {
                    biggestFaceView = faceView
                    biggestFaceViewArea = curArea
                }
            }

            biggestFaceView?.showClickAnim()

            set.applyTo(rootLayout)
        }
    }

    private fun getFaceView(): FaceView {
        return FaceView(requireContext()).apply {
            closeButtonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close_small_round)
            closeButtonSize = utils.dpToPx(35).toInt()
            borderWidth = utils.dpToPx(3)
            borderCornerRadius = utils.dpToPx(16)
            borderColor = Color.WHITE
            clickAnimSize = utils.dpToPx(60).toInt()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.closeImgView -> onBackPressed()
            R.id.muteImgView -> {
                if (isMuted) {
                    muteImgView.setImageResource(R.drawable.external_speaker)
                    exoPlayer.volume = currentVolume
                } else {
                    muteImgView.setImageResource(R.drawable.muted_speaker)
                    exoPlayer.volume = 0f
                }
                isMuted = !isMuted
            }
            R.id.avatarImgView -> produceResult()
            R.id.nameTextView -> produceResult()
        }
    }

    private fun produceResult() {
        processingDialog.show(childFragmentManager)

        if (bitmap != null) {
            viewModel.saveImage(bitmap!!) { uri ->
                activity().onFragmentResult(BaseActivity.FRAGMENT_EDIT_MEDIA, ImageMedia(uri))
            }
        } else {
            activity().onFragmentResult(BaseActivity.FRAGMENT_EDIT_MEDIA, VideoMedia(videoUrl!!))
        }
    }

    override fun onBackPressed(): Boolean {
        alertDialog.show(childFragmentManager,
                title = getString(R.string.label_discard_image),
                description = getString(R.string.description_discard_image),
                button1Text = getString(R.string.label_keep),
                button2Text = getString(R.string.label_discard),
                button2Action = {
                    activity.removeEditMediaFragment()
                }
        )
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.playWhenReady = false
    }
}