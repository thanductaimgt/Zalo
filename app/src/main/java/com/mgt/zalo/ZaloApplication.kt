package com.mgt.zalo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.mgt.zalo.di.AppComponent
import com.mgt.zalo.di.DaggerAppComponent
import com.mgt.zalo.manager.PermissionManager
import com.mgt.zalo.manager.SessionManager
import com.mgt.zalo.manager.SharedPrefsManager
import com.mgt.zalo.repository.Database
import com.mgt.zalo.repository.Storage
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
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
        appComponent = DaggerAppComponent.factory().create(this) as AppComponent
        return appComponent
    }

    override fun onCreate() {
        super.onCreate()

        if (sharedPrefsManager.isLogin() && permissionManager.hasRequiredPermissions()) {
            val user = sharedPrefsManager.getUser()
            sessionManager.initUser(user)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !sharedPrefsManager.isNotificationChannelsInit()) {
            createNotificationChannels()
            sharedPrefsManager.setNotificationChannelsInit(true)
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

    private fun createNotificationChannels() {
        //chat
        createNotificationChannel(Constants.CHAT_NOTIFY_CHANNEL_ID, Constants.CHAT_NOTIFY_CHANNEL_NAME, importance = NotificationManager.IMPORTANCE_HIGH)
        //upload post
        createNotificationChannel(Constants.UPLOAD_NOTIFY_CHANNEL_ID, Constants.UPLOAD_NOTIFY_CHANNEL_NAME, importance = NotificationManager.IMPORTANCE_HIGH)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
            channelId: String, channelName: String,
            lockScreenVisibility: Int = NotificationCompat.VISIBILITY_PUBLIC,
            importance: Int = NotificationManager.IMPORTANCE_DEFAULT
    ) {
        val chan = NotificationChannel(
                channelId,
                channelName, importance
        )
        chan.lightColor = getColor(R.color.lightPrimary)
        chan.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
    }

    companion object {
        lateinit var appComponent: AppComponent
    }
}