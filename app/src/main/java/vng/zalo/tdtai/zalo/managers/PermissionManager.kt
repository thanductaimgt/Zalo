package vng.zalo.tdtai.zalo.managers

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface PermissionManager {
    fun hasRequiredPermissions(context: Context): Boolean
    fun requestRequiredPermissions(activity: Activity)

    companion object {
        const val PERMISSIONS_REQUEST = 1234
    }
}

class PermissionManagerImpl @Inject constructor() : PermissionManager {
    override fun hasRequiredPermissions(context: Context): Boolean {
        var hasPermissions = true
        for (p in allPermissionsId) {
            hasPermissions =
                    hasPermissions && ContextCompat.checkSelfPermission(
                            context,
                            p
                    ) == PermissionChecker.PERMISSION_GRANTED
        }

        return hasPermissions
    }

    override fun requestRequiredPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
                activity,
                allPermissionsId,
                PermissionManager.PERMISSIONS_REQUEST
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
                Manifest.permission.RECEIVE_BOOT_COMPLETED
        )
    }
}