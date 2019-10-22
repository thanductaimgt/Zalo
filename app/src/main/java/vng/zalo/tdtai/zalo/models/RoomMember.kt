package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp

data class RoomMember(
        var phone: String? = null,
        var avatarUrl: String? = null,
        var joinDate: Timestamp? = null
){
    companion object{
        const val FIELD_PHONE = "phone"
        const val FIELD_AVATAR_URL = "avatarUrl"
    }
}