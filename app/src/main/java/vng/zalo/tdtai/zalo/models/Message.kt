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
            put("content", content)
            put("createdTime", createdTime)
            put("senderPhone", senderPhone)
            put("type", type)
        }
    }

    companion object{
        const val PAYLOAD_TIME = 0
        const val PAYLOAD_DATE = 1
    }
}