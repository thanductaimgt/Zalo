package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.R
import java.util.*

class CallMessage(
        id: String? = null,
        content: String? = null,// url if message is a sticker or image
        createdTime: Timestamp? = null,
        senderPhone: String? = null,
        senderAvatarUrl: String? = null,
        type: Int? = null,
        ratio: String? = null,
        fileSize: Long? = null,// for file only
        fileName: String? = null,// for file only
        var callTime: Int = 0,//call time in second
        var callType: Int = CALL_TYPE_VOICE,
        var isMissed: Boolean = false,
        var isCanceled: Boolean = false
) : Message(id, content, createdTime, senderPhone, senderAvatarUrl, type, ratio, fileSize, fileName) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            put(FIELD_CALL_TIME, callTime)
            put(FIELD_CALL_TYPE, callType)
            put(FIELD_IS_MISSED, isMissed)
            put(FIELD_IS_CANCELED, isCanceled)
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getLong(FIELD_CALL_TIME)?.let { callTime = it.toInt() }
        doc.getLong(FIELD_CALL_TYPE)?.let { callType = it.toInt() }
        doc.getBoolean(FIELD_IS_MISSED)?.let { isMissed = it }
        doc.getBoolean(FIELD_IS_CANCELED)?.let { isCanceled = it }
    }

    override fun getPreviewContent(context: Context): String {
        return "[${
        when {
            isMissed -> context.getString(R.string.description_missed_call)
            callType == CALL_TYPE_VOICE -> context.getString(R.string.description_voice_call)
            else -> context.getString(R.string.description_video_call)
        }
        }]"
    }

    companion object {
        const val FIELD_CALL_TIME = "callTime"
        const val FIELD_CALL_TYPE = "callType"
        const val FIELD_IS_MISSED = "isMissed"
        const val FIELD_IS_CANCELED = "isCanceled"

        const val CALL_TYPE_VOICE = 0
        const val CALL_TYPE_VIDEO = 1
    }
}