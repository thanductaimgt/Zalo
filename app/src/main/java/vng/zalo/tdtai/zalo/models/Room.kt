package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp

data class Room(var id: String? = null,
                var avatarUrl: String? = null,
                var createdTime: Timestamp? = null,
                var name: String? = null,
                var memberMap: Map<String, RoomMember>? = null) {
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            put(FIELD_CREATED_TIME, createdTime!!)
        }
    }

    companion object{
        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_TYPING_MEMBERS_PHONE = "typingMembersPhone"
    }
}