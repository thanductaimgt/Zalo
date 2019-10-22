package vng.zalo.tdtai.zalo

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.models.UserInfo
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG


class ZaloApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE)

        val isLogin = prefs.getBoolean(UserInfo.FIELD_IS_LOGIN, false)
        if (isLogin) {
            initUserInfo(prefs)
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

    private fun initUserInfo(prefs: SharedPreferences) {
        prefs.apply {
            currentUser = UserInfo(
                    phone = getString(UserInfo.FIELD_PHONE, null),
                    avatarUrl = getString(UserInfo.FIELD_AVATAR_URL, null),
                    birthDate = Timestamp(getLong(UserInfo.FIELD_BIRTH_DATE_SECS, 0), getInt(UserInfo.FIELD_BIRTH_DATE_NANO_SECS, 0)),
                    isMale = getBoolean(UserInfo.FIELD_IS_MALE, true),
                    joinDate = Timestamp(getLong(UserInfo.FIELD_JOIN_DATE_SECS, 0), getInt(UserInfo.FIELD_JOIN_DATE_NANO_SECS, 0))
            )
        }
    }

    inner class ZaloProcessLifecycleObserver : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Log.d(TAG, "onStart")
            currentUser?.phone?.let {
                Database.setCurrentUserOnlineState(true)
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            Log.d(TAG, "onStop")
            currentUser?.phone?.let {
                Database.setCurrentUserOnlineState(false)
            }
        }
    }

    companion object {
        var currentUser: UserInfo? = null
        var currentRoomId: String? = null
    }
}