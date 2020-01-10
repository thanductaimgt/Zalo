package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

data class FileMessage(
        override var id: String? = null,
        override var createdTime: Timestamp? = null,
        override var senderPhone: String? = null,
        override var senderAvatarUrl: String? = null,
        override var type: Int? = null,
        var url: String? = null,
        var fileSize: Long? = null,
        var fileName: String? = null
) : Message(id, createdTime, senderPhone, senderAvatarUrl, type) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            put(FIELD_URL, url)
            put(FIELD_FILE_SIZE, fileSize)
            put(FIELD_FILE_NAME, fileName)
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_URL)?.let { url = it }
        doc.getLong(FIELD_FILE_SIZE)?.let { fileSize = it }
        doc.getString(FIELD_FILE_NAME)?.let { fileName = it }
    }

    override fun getPreviewContent(context: Context): String {
        return "[File] $fileName"
    }

    companion object {
        const val FIELD_URL = "url"
        const val FIELD_FILE_SIZE = "fileSize"
        const val FIELD_FILE_NAME = "fileName"
    }
}