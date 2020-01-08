package vng.zalo.tdtai.zalo.views.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import vng.zalo.tdtai.zalo.PermissionManager
import vng.zalo.tdtai.zalo.SharedPrefsManager
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.utils.TAG


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PermissionManager.hasRequiredPermissions(this)) {
            moveToNextActivity()
        } else {
            PermissionManager.requestRequiredPermissions(this)
        }
    }

    private fun moveToNextActivity() {
        startActivity(Intent(this,
                if (SharedPrefsManager.isLogin(this)) {
                    if (!ZaloApplication.isUserInit()) {
                        val user = SharedPrefsManager.getUser(this)
                        ZaloApplication.initUser(this, user)
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
