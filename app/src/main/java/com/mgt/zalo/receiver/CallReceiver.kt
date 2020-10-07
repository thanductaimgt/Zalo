package com.mgt.zalo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mgt.zalo.ui.call.CallActivity
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Constants.ACTION_CALL -> {
                context.startActivity(
                        Intent(context, CallActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                            putExtras(intent.extras!!)
                        }
                )
                Log.d(TAG, "startActivity: $intent")
            }
        }
    }
}
