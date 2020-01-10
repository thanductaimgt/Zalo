package vng.zalo.tdtai.zalo.abstracts

import android.content.Context
import android.content.Intent
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.views.activities.CallActivity

object CallStarter {
    fun startAudioCall(context: Context, roomId: String, roomName:String, roomAvatarUrl:String){
        context.startActivity(Intent(context, CallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)

            putExtra(Constants.IS_CALLER, true)

            putExtra(Constants.ROOM_ID, roomId)
            putExtra(Constants.ROOM_NAME, roomName)
            putExtra(Constants.ROOM_AVATAR, roomAvatarUrl)
        })
    }
}