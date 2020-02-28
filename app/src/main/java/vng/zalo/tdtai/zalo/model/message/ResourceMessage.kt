package vng.zalo.tdtai.zalo.model.message

import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

abstract class ResourceMessage(
        id: String? = null,
        createdTime: Long? = null,
        senderPhone: String? = null,
        senderAvatarUrl: String? = null,
        type: Int? = null,
        isSent:Boolean=false,
        open var url: String = "unknown",
        open var uploadProgress: Int? = null,
        open var size: Long = -1
) : Message(id, createdTime, senderPhone, senderAvatarUrl, type, isSent) {
    override fun toMap(): HashMap<String, Any> {
        return super.toMap().also {
            it[FIELD_URL] = url
            it[FIELD_SIZE] = size
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_URL)?.let { url = it }
        doc.getLong(FIELD_SIZE)?.let { size = it }
    }

    companion object {
        const val FIELD_URL = "url"
        const val FIELD_SIZE = "size"
    }
}