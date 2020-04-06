package vng.zalo.tdtai.zalo.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.common.AlertDialog
import vng.zalo.tdtai.zalo.common.ProcessingDialog
import vng.zalo.tdtai.zalo.manager.*
import vng.zalo.tdtai.zalo.repository.Database
import vng.zalo.tdtai.zalo.repository.Storage
import vng.zalo.tdtai.zalo.service.NotificationService
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.Utils
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
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var isOnTop: Boolean = false

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (isOnTop) {
            bringToFront()
        }
        initAll()
    }

    // return true if back event has been handled
    open fun onBackPressed(): Boolean {
        return false
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

    fun activity():BaseActivity{
        return requireActivity() as BaseActivity
    }
}