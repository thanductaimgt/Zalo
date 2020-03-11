package vng.zalo.tdtai.zalo

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import vng.zalo.tdtai.zalo.di.DaggerAppComponent
import vng.zalo.tdtai.zalo.managers.PermissionManager
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.managers.SharedPrefsManager
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.repo.Storage
import vng.zalo.tdtai.zalo.utils.TAG
import javax.inject.Inject

class ZaloApplication : DaggerApplication() {
    @Inject
    lateinit var database: Database
    @Inject
    lateinit var storage: Storage

    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var sharedPrefsManager: SharedPrefsManager
    @Inject
    lateinit var permissionManager: PermissionManager

    //MultiDex
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (sharedPrefsManager.isLogin(this) && permissionManager.hasRequiredPermissions(this)) {
            val user = sharedPrefsManager.getUser(this)
            sessionManager.initUser(user)
        }

        observeProcessLifeCycle()
    }

    private fun observeProcessLifeCycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(ZaloProcessLifecycleObserver())
    }

    inner class ZaloProcessLifecycleObserver : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Log.d(TAG, "onStart")
            sessionManager.curUser?.phone?.let {
                database.setCurrentUserOnlineState(true)
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            Log.d(TAG, "onStop")
            sessionManager.curUser?.phone?.let {
                database.setCurrentUserOnlineState(false)
            }
        }
    }
}