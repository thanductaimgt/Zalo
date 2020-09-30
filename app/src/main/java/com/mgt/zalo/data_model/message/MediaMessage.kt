package com.mgt.zalo.data_model.message

import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

abstract class MediaMessage(
        id: String?,
        createdTime: Long?,
        senderPhone: String?,
        senderAvatarUrl: String?,
        type: Int?,
        isSent:Boolean,
        open var url: String,
        open var uploadProgress: Int?,
        open var size: Long,
        open var ratio: String?
) : Message(id, createdTime, senderPhone, senderAvatarUrl, type, isSent) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().also {
            it[FIELD_URL] = url
            it[FIELD_SIZE] = size
            it[FIELD_RATIO] = ratio
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_URL)?.let { url = it }
        doc.getLong(FIELD_SIZE)?.let { size = it }
        doc.getString(FIELD_RATIO)?.let { ratio = it }
    }

    companion object {
        const val FIELD_URL = "url"
        const val FIELD_SIZE = "size"
        const val FIELD_RATIO = "ratio"
    }
}