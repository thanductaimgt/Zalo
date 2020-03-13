package vng.zalo.tdtai.zalo.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import dagger.android.support.DaggerAppCompatActivity
import vng.zalo.tdtai.zalo.managers.PermissionManager
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.managers.SharedPrefsManager
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.ui.intro.IntroActivity
import vng.zalo.tdtai.zalo.utils.TAG
import javax.inject.Inject


class SplashActivity : DaggerAppCompatActivity() {
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var sharedPrefsManager: SharedPrefsManager
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (permissionManager.hasRequiredPermissions()) {
            moveToNextActivity()
        } else {
            permissionManager.requestRequiredPermissions(this)
        }
    }

    private fun moveToNextActivity() {
        startActivity(Intent(this,
                if (sharedPrefsManager.isLogin()) {
                    if (!sessionManager.isUserInit()) {
                        val user = sharedPrefsManager.getUser()
                        sessionManager.initUser(user)
                    }
                    HomeActivity::class.java
                } else {
                    IntroActivity::class.java
                }
        ))

        finish()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            PermissionManager.PERMISSIONS_REQUEST -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    moveToNextActivity()
                } else {
                    Log.e(TAG, "request permissions fail")
                    finish()
                }
            }
            else -> Log.d(TAG, "unknown requestCode: $requestCode")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
