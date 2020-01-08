package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.R
import java.util.*

abstract class Message(
        open var id:String?=null,
        open var createdTime: Timestamp? = null,
        open var senderPhone: String? = null,
        open var senderAvatarUrl: String? = null,
        open var type: Int? = null
) {
    open fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            createdTime?.let { put(FIELD_CREATED_TIME, it) }
            senderPhone?.let { put(FIELD_SENDER_PHONE, it) }
            type?.let { put(FIELD_TYPE, it) }
        }
    }

    abstract fun getPreviewContent(context: Context): String

    open fun applyDoc(doc:DocumentSnapshot){
        id = doc.id
        doc.getTimestamp(FIELD_CREATED_TIME)?.let{createdTime = it}
        doc.getString(FIELD_SENDER_PHONE)?.let{senderPhone = it}
        doc.getLong(FIELD_TYPE)?.let{type = it.toInt()}
    }

    companion object {
        const val PAYLOAD_TIME = 0
        const val PAYLOAD_AVATAR = 2

        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_STICKER = 2
        const val TYPE_FILE = 3
        const val TYPE_VIDEO = 4
        const val TYPE_TYPING = 5
        const val TYPE_CALL = 6

        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_SENDER_PHONE = "senderPhone"
        const val FIELD_TYPE = "type"
    }
}