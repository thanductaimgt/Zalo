package vng.zalo.tdtai.zalo.base

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.common.AlertDialog
import vng.zalo.tdtai.zalo.common.ProcessingDialog
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.manager.*
import vng.zalo.tdtai.zalo.repository.Database
import vng.zalo.tdtai.zalo.repository.Storage
import vng.zalo.tdtai.zalo.service.NotificationService
import vng.zalo.tdtai.zalo.ui.camera.CameraFragment
import vng.zalo.tdtai.zalo.ui.edit_media.EditMediaFragment
import vng.zalo.tdtai.zalo.ui.media.MediaFragment
import vng.zalo.tdtai.zalo.ui.profile.ProfileFragment
import vng.zalo.tdtai.zalo.ui.story.StoryFragment
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.Utils
import javax.inject.Inject


abstract class BaseActivity : DaggerAppCompatActivity(), BaseOnEventListener, BaseView {
    @Inject
    lateinit var application: ZaloApplication

    @Inject
    lateinit var sharedPrefsManager: SharedPrefsManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var externalIntentManager: ExternalIntentManager

    @Inject
    lateinit var messageManager: MessageManager

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var database: Database

    @Inject
    lateinit var storage: Storage

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var callService: CallService

    @Inject
    lateinit var alertDialog: AlertDialog

    @Inject
    lateinit var processingDialog: ProcessingDialog

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var backgroundWorkManager: BackgroundWorkManager

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        initAll()
    }

//    override fun onStart() {
//        super.onStart()
//        val rootView = findViewById<View>(android.R.id.content)
//        touchAnyWhereToDismissKeyboard(rootView)
//    }

//    fun touchAnyWhereToDismissKeyboard(view: View) {
//        // Set up touch listener for non-text box views to hide keyboard.
//        if (view !is EditText) {
//            view.setOnTouchListener { _, _ ->
//                utils.hideKeyboard(view)
//                false
//            }
//        }
//
//        //If a layout container, iterate over children and seed recursion.
//        if (view is ViewGroup) {
//            for (i in 0 until view.childCount) {
//                val innerView = view.getChildAt(i)
//                touchAnyWhereToDismissKeyboard(innerView)
//            }
//        }
//    }

    protected fun requestFullScreen() {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    var isStatusBarHidden = false

    fun showStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        isStatusBarHidden = false
    }

    fun hideStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        isStatusBarHidden = true
    }

    fun setContentView(@LayoutRes resId: Int, doMakeRoomForStatusBar: Boolean = false) {
        val rootView = layoutInflater.inflate(resId, null)
        if (doMakeRoomForStatusBar) {
            makeRoomForStatusBar(this, rootView)
        }
        setContentView(rootView)
    }

    fun setStatusBarMode(isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = if (isLight) {
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }

    final override fun onBackPressed() {
        if (!onBackPressedCustomized()) {
            super.onBackPressed()
        }
    }

    // return true if back event has been handled
    open fun onBackPressedCustomized(): Boolean {
        return if (supportFragmentManager.backStackEntryCount > 0) {
            val fragmentTag = supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name
            val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)

            if (fragment !is BaseFragment || !fragment.onBackPressed()) {
                supportFragmentManager.popBackStack()
            }
            true
        } else {
            false
        }
    }

    fun addFragment(fragment: BaseFragment, addToBackStack: Boolean = true, @IdRes parentId: Int? = null): Fragment {
        fragment.isOnTop = true

        val parentView = parentId?.let { findViewById<ViewGroup>(parentId) }
                ?: findViewById<ViewGroup>(android.R.id.content).getChildAt(0)

        supportFragmentManager.beginTransaction()
                .add(parentView.id, fragment, fragment.getInstanceTag())
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .apply {
                    if (addToBackStack) {
                        addToBackStack(fragment.getInstanceTag())
                    }
                }
                .commit()
        utils.hideKeyboard(parentView)
        return fragment
    }

    private fun removeFragment(tag: String, popBackStack: Boolean = true) {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                    .remove(fragment)
                    .setTransition(FragmentTransaction.TRANSIT_NONE)
                    .commit()

            if (popBackStack) {
                supportFragmentManager.popBackStack()
            }
        }
    }

    fun addStoryFragment(curStoryGroup: StoryGroup, storyGroups: List<StoryGroup>): StoryFragment {
        application.liveShouldReleaseVideoFocus.value = true

//        var fragment = supportFragmentManager.findFragmentByTag((null as StoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = StoryFragment(this, curStoryGroup, storyGroups)
        return addFragment(fragment) as StoryFragment
//        }
    }

    fun removeStoryFragment() {
        application.liveShouldReleaseVideoFocus.value = false

        removeFragment((null as StoryFragment?).TAG)
    }

    fun addEditMediaFragment(bitmap: Bitmap): EditMediaFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = EditMediaFragment(bitmap)
        return addFragment(fragment) as EditMediaFragment
//        }
    }

    fun addEditMediaFragment(videoUri: String): EditMediaFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = EditMediaFragment(videoUri)
        return addFragment(fragment) as EditMediaFragment
//        }
    }

    fun removeEditMediaFragment() {
        removeFragment((null as EditMediaFragment?).TAG)
    }

    fun addMediaFragment(curMessage: Message, messages: List<Message>): MediaFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as MediaFragment?).TAG)
//        if (fragment == null) {
        val fragment = MediaFragment(this, curMessage, messages)
        return addFragment(fragment) as MediaFragment
//        }
    }

    fun removeMediaFragment() {
        removeFragment((null as MediaFragment?).TAG)
    }

    fun addProfileFragment(userId: String): ProfileFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as ProfileFragment?).TAG)
//        if (fragment == null) {
        val fragment = ProfileFragment(userId)
        return addFragment(fragment) as ProfileFragment
//        }
    }

    fun removeProfileFragment() {
        removeFragment((null as ProfileFragment?).TAG)
    }

    fun addCameraFragment(): CameraFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = CameraFragment()
        return addFragment(fragment) as CameraFragment
//        }
    }

    fun removeCameraFragment() {
        removeFragment((null as CameraFragment?).TAG)
    }

    final override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super<DaggerAppCompatActivity>.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            onActivityResult(requestCode, intent)
        } else {
            Log.e(TAG, "resultCode is not OK")
        }
    }

    open fun onFragmentResult(fragmentType: Int, result: Any?) {}

//    override fun onDestroy() {
//        findViewById<ViewGroup>(android.R.id.content).getChildAt(0)?.let {
//            it.findViewById<EditText>(R.id.msgEditText)?.let {
//                utils.hideKeyboard(it)
//            }
//        }
//        super.onDestroy()
//    }

    companion object {
        const val FRAGMENT_EDIT_MEDIA = 0
        const val FRAGMENT_CAMERA = 1
    }
}