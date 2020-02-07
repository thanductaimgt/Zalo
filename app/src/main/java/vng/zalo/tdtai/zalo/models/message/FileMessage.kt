package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.R
import java.util.*

data class FileMessage(
        override var id: String? = null,
        override var createdTime: Timestamp? = null,
        override var senderPhone: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false,
        override var url: String = "unknown",
        override var uploadProgress:Int?=null,
        override var size:Long=-1,
        var fileName: String? = null
) : ResourceMessage(id, createdTime, senderPhone, senderAvatarUrl, TYPE_FILE, isSent, url, uploadProgress, size) {
    override fun toMap(): HashMap<String, Any> {
        return super.toMap().apply {
            fileName?.let { put(FIELD_FILE_NAME, it) }
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_FILE_NAME)?.let { fileName = it }
    }

    override fun getPreviewContent(context: Context): String {
        return "[File] ${fileName?:context.getString(R.string.label_no_name)}"
    }

    companion object {
        const val FIELD_FILE_NAME = "fileName"
    }
}