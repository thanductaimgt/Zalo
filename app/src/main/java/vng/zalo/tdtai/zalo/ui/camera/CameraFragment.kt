package vng.zalo.tdtai.zalo.ui.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.bottom_sheet_choose_gallery.view.*
import kotlinx.android.synthetic.main.fragment_camera.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.manager.ExternalIntentManager
import vng.zalo.tdtai.zalo.util.Constants
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.setOnDoubleClickListener
import vng.zalo.tdtai.zalo.util.toBitmap

@SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
class CameraFragment : BaseFragment() {
    lateinit var bottomSheetDialog: BottomSheetDialog

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture: ImageCapture
    private lateinit var preview: Preview
    private var isCameraFront = true

    var isCameraBinding = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (isOnTop) {
            activity().hideStatusBar()
        }
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onResume() {
        super.onResume()
        bindUseCases()
    }

    override fun onPause() {
        super.onPause()
        unbindAllUseCases()
    }


    override fun onBindViews() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//        cameraProviderFuture.addListener(Runnable {
//            bindUseCases(cameraProviderFuture.get())
//        }, ContextCompat.getMainExecutor(requireContext()))
        cancelImgView.setOnClickListener(this)
        captureImgView.setOnClickListener(this)
        galleryImgView.setOnClickListener(this)
        toggleCameraImgView.setOnClickListener(this)

        previewView.setOnDoubleClickListener {
            toggleCamera()
        }

        initBottomSheet()
    }

    private fun initBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(activity()).apply {
            // Fix BottomSheetDialog not showing after getting hidden when the user drags it down
            setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                val frameLayout = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
                BottomSheetBehavior.from(frameLayout).apply {
                    skipCollapsed = true
                }
            }

            val rootView = layoutInflater.inflate(R.layout.bottom_sheet_choose_gallery, null)
            rootView.uploadImageTV.setOnClickListener(this@CameraFragment)
            rootView.uploadVideoTV.setOnClickListener(this@CameraFragment)

            setContentView(rootView)
        }
    }

    override fun onViewsBound() {
        isCameraFront = sharedPrefsManager.getCameraSide()
    }

    fun bindUseCases() {
        if (!isCameraBinding) {
            isCameraBinding = true
            rootView.post {
                Log.d(TAG, "bindUseCases")
                val cameraSelector: CameraSelector = CameraSelector.Builder()
                        .requireLensFacing(if (isCameraFront) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
                        .build()

                val resolution = Size(rootView.width, rootView.height)

                //preview
                preview = Preview.Builder()
                        .setTargetResolution(resolution)
                        .build()
                        .apply { setSurfaceProvider(previewView.previewSurfaceProvider) }

                imageCapture = ImageCapture.Builder()
                        .setTargetResolution(resolution)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()

                cameraProviderFuture.get().bindToLifecycle(viewLifecycleOwner, cameraSelector, imageCapture, preview)
            }
        }
    }

    fun unbindAllUseCases() {
        Log.d(TAG, "unbind all")

        if (isCameraBinding) {
            CameraX.unbindAll()
            isCameraBinding = false
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.cancelImgView -> {
                activity().onFragmentResult(BaseActivity.FRAGMENT_CAMERA, null)
            }
            R.id.captureImgView -> {
                imageCapture.takePicture(HandlerExecutor(Looper.getMainLooper()),
                        object : ImageCapture.OnImageCapturedCallback() {
                            @SuppressLint("UnsafeExperimentalUsageError")
                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                val ratio = rootView.width / rootView.height.toFloat()
                                val bitmap = utils.rotateImage(imageProxy.toBitmap(), imageProxy.imageInfo.rotationDegrees, isCameraFront, ratio)
                                activity().addEditMediaFragment(bitmap)
                                super.onCaptureSuccess(imageProxy)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                super.onError(exception)
                                Toast.makeText(requireContext(), "error while taking photo", Toast.LENGTH_SHORT).show()
                            }
                        })
            }
            R.id.toggleCameraImgView -> toggleCamera()
            R.id.galleryImgView -> {
                bottomSheetDialog.show()
            }
            R.id.uploadImageTV -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_IMAGE_REQUEST, ExternalIntentManager.CHOOSER_TYPE_IMAGE, false)
            }
            R.id.uploadVideoTV -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_VIDEO_REQUEST, ExternalIntentManager.CHOOSER_TYPE_VIDEO, false)
            }
        }
    }

    private fun toggleCamera() {
        isCameraFront = !isCameraFront
        sharedPrefsManager.setCameraSide(isCameraFront)
        unbindAllUseCases()
        bindUseCases()
    }

    override fun onActivityResult(requestCode: Int, intent: Intent?) {
        utils.assertNotNull(intent, TAG, "onActivityResult.intent") { intentNotNull ->
            val uri = intentNotNull.data!!
            when (requestCode) {
                Constants.CHOOSE_IMAGE_REQUEST -> {
                    val bitmap = resourceManager.getImageBitmap(uri.toString())
                    activity().addEditMediaFragment(bitmap)
                }
                Constants.CHOOSE_VIDEO_REQUEST -> {
                    activity().addEditMediaFragment(uri.toString())
                }
            }
        }
        bottomSheetDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isOnTop) {
            activity().showStatusBar()
        }
    }
}