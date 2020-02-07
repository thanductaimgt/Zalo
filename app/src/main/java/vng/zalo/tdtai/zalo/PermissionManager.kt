package vng.zalo.tdtai.zalo

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class PermissionManager {
    companion object{
        fun hasRequiredPermissions(context: Context): Boolean {
            var hasPermissions = true
            for (p in permissionsId) {
                hasPermissions =
                        hasPermissions && ContextCompat.checkSelfPermission(
                                context,
                                p
                        ) == PermissionChecker.PERMISSION_GRANTED
            }

            return hasPermissions
        }

        fun requestRequiredPermissions(activity:Activity){
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsId,
                    PERMISSIONS_REQUEST
            )
        }

        const val PERMISSIONS_REQUEST = 1234

        private val permissionsId = arrayOf(
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