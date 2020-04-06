package vng.zalo.tdtai.zalo.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.manager.PermissionManager
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.ui.intro.IntroActivity
import vng.zalo.tdtai.zalo.util.TAG


class SplashActivity : BaseActivity() {
    override fun onViewsBound() {
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
