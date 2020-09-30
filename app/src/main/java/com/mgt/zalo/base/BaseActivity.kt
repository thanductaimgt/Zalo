package com.mgt.zalo.base

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.common.AlertDialog
import com.mgt.zalo.common.ProcessingDialog
import com.mgt.zalo.manager.*
import com.mgt.zalo.repository.Database
import com.mgt.zalo.repository.Storage
import com.mgt.zalo.service.NotificationService
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.Utils
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
    lateinit var zaloFragmentManager: ZaloFragmentManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var backgroundWorkManager: BackgroundWorkManager

    @Inject lateinit var playbackManager: PlaybackManager

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        zaloFragmentManager.init(supportFragmentManager, this)
        onBindViews()
        val rootView = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        zaloFragmentManager.setRootView(rootView)
        onViewsBound()
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
//        window.addFlags(Window.FEATURE_ACTION_BAR_OVERLAY)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    var isStatusBarHidden = false

    fun showStatusBar() {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
//        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        isStatusBarHidden = false
    }

    fun hideStatusBar() {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_FULLSCREEN
//        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
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

    override fun onBackPressed() {
        if(!zaloFragmentManager.popTopFragment()){
            onBackPressedCustomized()
        }
    }

    open fun onBackPressedCustomized() {
        super<DaggerAppCompatActivity>.onBackPressed()
    }

    final override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super<DaggerAppCompatActivity>.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            onActivityResult(requestCode, intent)
        } else {
            Log.e(TAG, "resultCode is not OK")
        }
    }

//    override fun onDestroy() {
//        findViewById<ViewGroup>(android.R.id.content).getChildAt(0)?.let {
//            it.findViewById<EditText>(R.id.msgEditText)?.let {
//                utils.hideKeyboard(it)
//            }
//        }
//        super.onDestroy()
//    }
}