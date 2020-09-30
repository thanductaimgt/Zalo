package com.mgt.zalo.data_model

import com.google.firebase.firestore.DocumentSnapshot

data class RoomMember(
        var userId: String? = null,
        var avatarUrl: String? = null,
        var joinDate: Long? = null,
        var name:String?=null
):BaseDataModel{
    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            userId?.let{put(FIELD_ID, it)}
            avatarUrl?.let{put(FIELD_AVATAR_URL, it)}
            joinDate?.let{put(FIELD_JOIN_DATE, it)}
            name?.let{put(FIELD_NAME, it)}
        }
    }

    private fun applyObject(any:Any) {
        @Suppress("UNCHECKED_CAST")
        any as HashMap<String, Any?>
        userId = any[FIELD_ID] as String?
        avatarUrl = any[FIELD_AVATAR_URL] as String?
        joinDate = any[FIELD_JOIN_DATE] as Long?
        name = any[FIELD_NAME] as String?
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        userId = doc.getString(FIELD_ID)
        avatarUrl = doc.getString(FIELD_AVATAR_URL)
        joinDate = doc.getLong(FIELD_JOIN_DATE)
        name = doc.getString(FIELD_NAME)
    }

    companion object{
        const val FIELD_ID = "userId"
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