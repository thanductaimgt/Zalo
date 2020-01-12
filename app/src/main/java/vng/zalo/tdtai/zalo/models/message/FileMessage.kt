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
        override var url: String = "unknown",
        override var uploadProgress:Int?=null,
        override var size:Long=-1,
        var fileName: String = "unnamed"
) : ResourceMessage(id, createdTime, senderPhone, senderAvatarUrl, type, url, uploadProgress, size) {
    override fun toMap(): HashMap<String, Any> {
        return super.toMap().apply {
            put(FIELD_FILE_NAME, fileName)
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_FILE_NAME)?.let { fileName = it }
    }

    override fun getPreviewContent(context: Context): String {
        return "[File] $fileName"
    }

    companion object {
        const val FIELD_FILE_NAME = "fileName"
    }
}