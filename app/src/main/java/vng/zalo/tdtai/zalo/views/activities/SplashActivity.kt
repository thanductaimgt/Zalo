package vng.zalo.tdtai.zalo.views.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import vng.zalo.tdtai.zalo.SharedPrefsManager
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasPermissions(Constants.permissionsId)) {
            moveToNextActivity()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    Constants.permissionsId,
                    Constants.PERMISSIONS_REQUEST
            )
        }
    }

    private fun moveToNextActivity() {
        startActivity(Intent(this,
                if (SharedPrefsManager.isLogin(this))
                    HomeActivity::class.java
                else
                    IntroActivity::class.java)
        )

        finish()
    }

    private fun hasPermissions(permissionsId: Array<String>): Boolean {
        var hasPermissions = true
        for (p in permissionsId) {
            hasPermissions =
                    hasPermissions && ContextCompat.checkSelfPermission(
                            this,
                            p
                    ) == PermissionChecker.PERMISSION_GRANTED
        }

        return hasPermissions
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.PERMISSIONS_REQUEST -> {
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
