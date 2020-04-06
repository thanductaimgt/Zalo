package vng.zalo.tdtai.zalo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import vng.zalo.tdtai.zalo.util.Constants
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.ui.call.CallActivity

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
