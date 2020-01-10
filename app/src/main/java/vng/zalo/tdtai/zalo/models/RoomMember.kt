package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp

data class RoomMember(
        var phone: String? = null,
        var avatarUrl: String? = null,
        var joinDate: Timestamp? = null
){
    fun toMap(): Map<String, Any?> {
        return HashMap<String, Any?>().apply {
            put(FIELD_PHONE, phone)
            put(FIELD_AVATAR_URL, avatarUrl)
            put(FIELD_JOIN_DATE, joinDate)
        }
    }

    companion object{
        const val FIELD_PHONE = "phone"
        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_JOIN_DATE = "joinDate"
    }
}