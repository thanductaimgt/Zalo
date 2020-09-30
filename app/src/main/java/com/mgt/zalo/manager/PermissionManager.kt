package com.mgt.zalo.manager

import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.mgt.zalo.ZaloApplication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
        private val application: ZaloApplication
) {
    fun hasRequiredPermissions(): Boolean {
        var hasPermissions = true
        for (p in allPermissionsId) {
            hasPermissions =
                    hasPermissions && ContextCompat.checkSelfPermission(
                            application,
                            p
                    ) == PermissionChecker.PERMISSION_GRANTED
        }

        return hasPermissions
    }

    fun requestRequiredPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
                activity,
                allPermissionsId,
                PERMISSIONS_REQUEST
        )
    }

    companion object {
        private val allPermissionsId = arrayOf(
                Manifest.permission.USE_SIP,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.CAMERA,
                Manifest.permission.FOREGROUND_SERVICE
        )

        const val PERMISSIONS_REQUEST = 1234
    }
}