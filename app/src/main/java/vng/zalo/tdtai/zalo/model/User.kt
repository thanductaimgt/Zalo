package vng.zalo.tdtai.zalo.model

import com.google.firebase.firestore.DocumentSnapshot

data class User(
        var phone: String? = null,// also id
        var name: String? = null,
        var avatarUrl: String? = null,
        var birthDate: Long? = null,
        var isMale: Boolean? = false,
        var joinDate: Long? = null,
        var lastOnlineTime: Long? = System.currentTimeMillis()//null means is online
) {
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            name?.let{put(FIELD_NAME, it)}
            avatarUrl?.let{put(FIELD_AVATAR_URL, it)}
            birthDate?.let{put(FIELD_BIRTH_DATE, it)}
            isMale?.let{put(FIELD_IS_MALE, it)}
            joinDate?.let{put(FIELD_JOIN_DATE, it)}
            lastOnlineTime?.let{put(FIELD_LAST_ONLINE_TIME, it)}
        }
    }

    private fun applyDoc(doc: DocumentSnapshot) {
        phone = doc.id
        doc.getString(FIELD_NAME)?.let { name = it }
        doc.getString(FIELD_AVATAR_URL)?.let { avatarUrl = it }
        doc.getLong(FIELD_BIRTH_DATE)?.let { birthDate = it }
        doc.getBoolean(FIELD_IS_MALE)?.let { isMale = it }
        doc.getLong(FIELD_JOIN_DATE)?.let { joinDate = it }
    }

    companion object {
        const val FIELD_PHONE = "phone"
        const val FIELD_NAME = "name"
        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_BIRTH_DATE = "birthDate"
        const val FIELD_IS_MALE = "isMale"
        const val FIELD_JOIN_DATE = "joinDate"
        const val FIELD_IS_LOGIN = "isLogin"
        const val FIELD_LAST_ONLINE_TIME = "lastOnlineTime"

        fun fromDoc(doc:DocumentSnapshot):User{
            return User().apply { applyDoc(doc) }
        }
    }
}