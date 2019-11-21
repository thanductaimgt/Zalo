package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.R
import java.util.*

class CallMessage(
        content: String? = null,// url if message is a sticker or image
        createdTime: Timestamp? = null,
        senderPhone: String? = null,
        senderAvatarUrl: String? = null,
        type: Int? = null,
        ratio: String? = null,
        fileSize: Long? = null,// for file only
        fileName: String? = null,// for file only
        var callTime:Int=0,//call time in second
        var callType:Int=CALL_TYPE_VOICE,
        var isMissed:Boolean=false,
        var isCanceled:Boolean=false
):Message(content, createdTime, senderPhone, senderAvatarUrl, type, ratio, fileSize, fileName) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            put(FIELD_CALL_TIME, callTime)
        }
    }

    override fun getPreviewContent(context: Context): String {
        return "[${context.getString(R.string.description_voice_call)}]"
    }

    companion object {
        const val FIELD_CALL_TIME = "callTime"
        const val CALL_TYPE_VOICE = 0
        const val CALL_TYPE_VIDEO = 1
    }
}