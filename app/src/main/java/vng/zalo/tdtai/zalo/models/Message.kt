package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp
import java.util.*

data class Message(var content: String? = null,// url if message is a sticker or image
                   var createdTime: Timestamp? = null,
                   var senderPhone: String? = null,
                   var senderAvatarUrl: String? = null,
                   var type: Int? = null) {
    fun toMap(): Map<String, Any?> {
        return HashMap<String, Any?>().apply {
            put(FIELD_CONTENT, content)
            put(FIELD_CREATED_TIME, createdTime)
            put(FIELD_SENDER_PHONE, senderPhone)
            put(FIELD_TYPE, type)
        }
    }

    fun getPreviewContent():String{
        return when (type) {
            TYPE_TEXT -> content!!
            TYPE_STICKER -> "[Sticker]"
            else -> ""
        }
    }

    companion object{
        const val PAYLOAD_TIME = 0
        const val PAYLOAD_AVATAR = 2

        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_STICKER = 2
        const val TYPE_FILE = 3
        const val TYPE_VOICE = 4
        const val TYPE_TYPING = 5

        const val FIELD_CONTENT = "content"
        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_SENDER_PHONE = "senderPhone"
        const val FIELD_TYPE = "type"
    }
}