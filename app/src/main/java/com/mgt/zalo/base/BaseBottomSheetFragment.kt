package com.mgt.zalo.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mgt.zalo.R
import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.common.AlertDialog
import com.mgt.zalo.common.ProcessingDialog
import com.mgt.zalo.manager.*
import com.mgt.zalo.repository.Database
import com.mgt.zalo.repository.Storage
import com.mgt.zalo.service.NotificationService
import com.mgt.zalo.util.ImageLoader
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.Utils
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


abstract class BaseBottomSheetFragment : BottomSheetDialogFragment(), BaseOnEventListener, BaseView, HasAndroidInjector {
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

    @Inject
    lateinit var playbackManager: PlaybackManager

    @Inject
    lateinit var imageLoader: ImageLoader

    var isOnTop: Boolean = false

    lateinit var parent: BaseView
    lateinit var parentZaloFragmentManager: ZaloFragmentManager

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any?>

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun androidInjector(): AndroidInjector<Any?>? {
        return androidInjector
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), R.style.AppTheme_Light_DialogStyle) {
            override fun onBackPressed() {
                if (!this@BaseBottomSheetFragment.onBackPressed()) {
                    super.onBackPressed()
                }
            }
        }
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return createView(inflater, container).apply {
            requireDialog().apply {
//                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                setOnShowListener { dialogInterface ->
                    val bottomSheetDialog = dialogInterface as BottomSheetDialog
                    val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
                    bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                    bottomSheet.background = null
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                }
            }
        }
    }

    abstract fun createView(inflater: LayoutInflater, container: ViewGroup?): View

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        zaloFragmentManager.init(childFragmentManager, this)
        zaloFragmentManager.setRootView(view)
        initAll()
    }

    // call only when added using ZaloFragmentManager
    fun init(parent: BaseView) {
        isOnTop = true
        this.parent = parent
        initParentFM()
    }

    private fun initParentFM() {
        val parent = parent
        parentZaloFragmentManager = when (parent) {
            is BaseActivity -> {
                parent.zaloFragmentManager
            }
            is BaseFragment -> {
                parent.fragmentManager()
            }
            else -> {
                (parent as BaseBottomSheetFragment).zaloFragmentManager
            }
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

    override fun onBackPressed(): Boolean {
        return zaloFragmentManager.popTopFragment() || onBackPressedCustomized()
    }

    open fun onBackPressedCustomized(): Boolean {
        return false
    }

    override fun onDestroy() {
//        activity().window.decorView.systemUiVisibility = systemUiVisibilityWhenInit!!
//        if (isStatusBarHiddenWhenInit) {
//            activity().hideStatusBar()
//        } else {
//            activity().showStatusBar()
//        }
        playbackManager.exoPlayer.stop()
        super.onDestroy()
    }
}