package vng.zalo.tdtai.zalo.zalo.models

import com.google.firebase.Timestamp
import java.util.*

data class Message(var content: String? = null,
                   var createdTime: Timestamp? = null,
                   var senderPhone: String? = null,
                   var senderAvatar: String?=null,
                   var type: Int? = null) {
    fun toMap(): Map<String, Any?> {
        return HashMap<String, Any?>().apply {
            put("content", content)
            put("createdTime", createdTime)
            put("senderPhone", senderPhone)
            put("type", type)
        }
    }
}