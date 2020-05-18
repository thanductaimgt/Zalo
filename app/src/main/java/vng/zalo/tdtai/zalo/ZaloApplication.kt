package vng.zalo.tdtai.zalo

import android.content.Context
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import vng.zalo.tdtai.zalo.di.DaggerAppComponent
import vng.zalo.tdtai.zalo.manager.PermissionManager
import vng.zalo.tdtai.zalo.manager.SessionManager
import vng.zalo.tdtai.zalo.manager.SharedPrefsManager
import vng.zalo.tdtai.zalo.repository.Database
import vng.zalo.tdtai.zalo.repository.Storage
import vng.zalo.tdtai.zalo.util.TAG
import javax.inject.Inject

class ZaloApplication : DaggerApplication(), CameraXConfig.Provider {
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

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    //MultiDex
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        appComponent =  DaggerAppComponent.factory().create(this) as DaggerAppComponent
        return appComponent
    }

    override fun onCreate() {
        super.onCreate()

        if (sharedPrefsManager.isLogin() && permissionManager.hasRequiredPermissions()) {
            val user = sharedPrefsManager.getUser()
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
            sessionManager.curUser?.id?.let {
                database.setCurrentUserOnlineState(true)
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            Log.d(TAG, "onStop")
            sessionManager.curUser?.id?.let {
                database.setCurrentUserOnlineState(false)
            }
        }
    }

    companion object{
        lateinit var appComponent: DaggerAppComponent
    }
}