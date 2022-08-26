package com.mgt.zalo.manager

import android.Manifest
import android.app.Activity
import android.os.Build
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
        private val allPermissionsId = arrayListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        const val PERMISSIONS_REQUEST = 1234
    }
}