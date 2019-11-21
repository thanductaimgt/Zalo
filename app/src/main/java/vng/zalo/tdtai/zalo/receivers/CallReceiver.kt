package vng.zalo.tdtai.zalo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.views.activities.CallActivity

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Constants.ACTION_CALL -> {
                context.startActivity(
                        Intent(context, CallActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtras(intent.extras!!)
                        }
                )
                Log.d(TAG, "startActivity: $intent")
            }
        }
    }
}
