package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*
import kotlin.collections.HashMap

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

    private fun applyObject(any:Any) {
        any as HashMap<String, Any?>
        phone = any[FIELD_PHONE] as String?
        avatarUrl = any[FIELD_AVATAR_URL] as String?
        joinDate = any[FIELD_JOIN_DATE] as Timestamp?
        name = any[FIELD_NAME] as String?
    }

    private fun applyDoc(doc: DocumentSnapshot) {
        phone = doc.getString(FIELD_PHONE)
        avatarUrl = doc.getString(FIELD_AVATAR_URL)
        joinDate = doc.getTimestamp(FIELD_JOIN_DATE)
        name = doc.getString(FIELD_NAME)
    }

    companion object{
        const val FIELD_PHONE = "phone"
        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_JOIN_DATE = "joinDate"
        const val FIELD_NAME = "name"

        fun fromObject(any:Any): RoomMember {
            return RoomMember().apply { applyObject(any) }
        }

        fun fromDoc(doc:DocumentSnapshot):RoomMember{
            return RoomMember().apply { applyDoc(doc) }
        }
    }
}