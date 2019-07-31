package vng.zalo.tdtai.zalo.zalo.models

import com.google.firebase.Timestamp

data class Room(var id: String? = null,
                var avatarUrl: String? = null,
                var createdTime: Timestamp? = null,
                var name: String? = null,
                var memberMap: Map<String, RoomMember>? = null) {
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            put("createdTime", createdTime!!)
        }
    }
}