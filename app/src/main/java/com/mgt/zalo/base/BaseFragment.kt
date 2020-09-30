package com.mgt.zalo.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import com.mgt.zalo.R
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

abstract class BaseFragment : DaggerFragment(), BaseOnEventListener, BaseView {
    @Inject
    lateinit var application: ZaloApplication

    @Inject
    lateinit var processingDialog: ProcessingDialog

    @Inject
    lateinit var sharedPrefsManager: SharedPrefsManager

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var externalIntentManager: ExternalIntentManager

    @Inject
    lateinit var messageManager: MessageManager

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var callService: CallService

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var database: Database

    @Inject
    lateinit var storage: Storage

    @Inject
    lateinit var alertDialog: AlertDialog

    @Inject
    lateinit var zaloFragmentManager: ZaloFragmentManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject lateinit var playbackManager: PlaybackManager

    var isOnTop: Boolean = false

    lateinit var parent: BaseView
    lateinit var parentZaloFragmentManager: ZaloFragmentManager

    // call only when added using ZaloFragmentManager
    fun init(parent: BaseView) {
        isOnTop = true
        this.parent = parent
        initParentFM()
    }

    private fun initParentFM() {
        this.parentZaloFragmentManager = if (parent is BaseActivity) {
            (parent as BaseActivity).zaloFragmentManager
        } else if(parent is BaseFragment){
            (parent as BaseFragment).fragmentManager()
        }else{
            (parent as BaseBottomSheetFragment).zaloFragmentManager
        }
    }

    private var isStatusBarHiddenWhenInit = false
    private var systemUiVisibilityWhenInit:Int? = null
    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isStatusBarHiddenWhenInit = activity().isStatusBarHidden
        systemUiVisibilityWhenInit = activity().window.decorView.systemUiVisibility
        activity().setStatusBarMode(true)
        val rootView = createView(inflater, container)
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            id = View.generateViewId()
            addView(rootView)
        }
    }

    abstract fun createView(inflater: LayoutInflater, container: ViewGroup?): View

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (isOnTop) {
            bringToFront()
        } else {
            parent =  (parentFragment as BaseView?) ?: activity()
            initParentFM()
        }
        zaloFragmentManager.init(childFragmentManager, this)
        zaloFragmentManager.setRootView(view)
        initAll()
    }

    private fun bringToFront() {
        view?.apply {
            elevation = resources.getDimension(R.dimen.sizeBottomNavigationElevation)
            isClickable = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            onActivityResult(requestCode, intent)
        } else {
            Log.e(TAG, "resultCode is not OK")
        }
    }

    open fun getInstanceTag(): String {
        return TAG
    }

    fun activity(): BaseActivity {
        return requireActivity() as BaseActivity
    }

    fun fragmentManager():ZaloFragmentManager{
        return if (isOnTop) {
            zaloFragmentManager
        } else {
            parentZaloFragmentManager
        }
    }

    // true if back handled, do not override this
    override fun onBackPressed(): Boolean {
        return zaloFragmentManager.popTopFragment() || onBackPressedCustomized()
    }

    open fun onBackPressedCustomized(): Boolean {
        return false
    }

    override fun onDestroy() {
        activity().window.decorView.systemUiVisibility = systemUiVisibilityWhenInit!!
        if (isStatusBarHiddenWhenInit) {
            activity().hideStatusBar()
        } else {
            activity().showStatusBar()
        }
        playbackManager.pause()
//        playbackManager.exoPlayer.stop()
        super.onDestroy()
    }
}