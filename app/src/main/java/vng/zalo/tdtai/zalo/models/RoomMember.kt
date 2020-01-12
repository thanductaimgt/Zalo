package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp

data class RoomMember(
        var phone: String? = null,
        var avatarUrl: String? = null,
        var joinDate: Timestamp? = null,
        var name:String?=null
){
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            phone?.let{put(FIELD_PHONE, it)}
            avatarUrl?.let{put(FIELD_AVATAR_URL, it)}
            joinDate?.let{put(FIELD_JOIN_DATE, it)}
            name?.let{put(FIELD_NAME, it)}
        }
    }

    companion object{
        const val FIELD_PHONE = "phone"
        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_JOIN_DATE = "joinDate"
        const val FIELD_NAME = "name"
    }
}