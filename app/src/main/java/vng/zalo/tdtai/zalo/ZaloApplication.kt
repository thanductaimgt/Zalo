package vng.zalo.tdtai.zalo

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.sip.*
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import vng.zalo.tdtai.zalo.models.User
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.receivers.CallReceiver
import vng.zalo.tdtai.zalo.services.NotificationService
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.views.fragments.AlertDialog


class ZaloApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        if (SharedPrefsManager.isLogin(this) && PermissionManager.hasRequiredPermissions(this)) {
            val user = SharedPrefsManager.getUser(this)
            initUser(this, user)
        }

        observeProcessLifeCycle()
//        val applicationComponent = DaggerApplicationComponent
//                .builder()
//                .applicationModule(ApplicationModule(this))
//                .build()
//                ApplicationComponent applicationComponent = DaggerApplicationComponent.create();
//        applicationComponent.inject(this)
    }

    private fun observeProcessLifeCycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(ZaloProcessLifecycleObserver())
    }

    inner class ZaloProcessLifecycleObserver : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Log.d(TAG, "onStart")
            curUser?.phone?.let {
                Database.setCurrentUserOnlineState(true)
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            Log.d(TAG, "onStop")
            curUser?.phone?.let {
                Database.setCurrentUserOnlineState(false)
            }
        }
    }

    companion object {
        var curUser: User? = null
        var currentRoomId: String? = null
        private val callReceiver: CallReceiver = CallReceiver()
        private val filter = IntentFilter().apply {
            addAction(Constants.ACTION_CALL)
        }

        var sipManager: SipManager? = null
        var sipProfile: SipProfile? = null

        var notificationDialog = AlertDialog()

        fun initUser(context: Context, user: User) {
            this.curUser = user
            initSip(context.applicationContext)
            registerCallReceiver(context.applicationContext)
            Database.setCurrentUserOnlineState(true)
        }

        fun removeCurrentUser(context: Context) {
            this.curUser = null
            removeSip()
            unregisterCallReceiver(context.applicationContext)
            context.stopService(Intent(context.applicationContext, NotificationService::class.java))
            Database.setCurrentUserOnlineState(false)
        }

        fun isUserInit(): Boolean {
            return curUser != null
        }

        private fun registerCallReceiver(context: Context) {
            context.registerReceiver(callReceiver, filter)
        }

        private fun initSip(context: Context) {
            sipProfile = SipProfile.Builder("${Constants.SIP_ACCOUNT_PREFIX}${curUser!!.phone}", Constants.SIP_DOMAIN)
                    .setPassword(curUser!!.phone)
                    .build()

            sipManager = SipManager.newInstance(context)

            val intent = Intent(Constants.ACTION_CALL)
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, intent, Intent.FILL_IN_DATA)

            sipManager?.open(sipProfile, pendingIntent, object : SipRegistrationListener {
                override fun onRegistering(localProfileUri: String?) {
                    Log.d(TAG, "open: onRegistering")
                }

                override fun onRegistrationDone(localProfileUri: String?, expiryTime: Long) {
                    Log.d(TAG, "open: onRegistrationDone")
                }

                override fun onRegistrationFailed(localProfileUri: String?, errorCode: Int, errorMessage: String?) {
                    Log.d(TAG, "open: onRegistrationFailed")
                }
            })
            sipManager?.isRegistered(sipProfile!!.uriString)
        }

        private fun removeSip() {
            try {
                sipManager?.close(sipProfile?.uriString)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to close local profile")
                t.printStackTrace()
            }
        }

        private fun unregisterCallReceiver(context: Context) {
            sipManager?.let { context.unregisterReceiver(callReceiver) }
        }
    }
}