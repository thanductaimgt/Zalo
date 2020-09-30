package com.mgt.zalo.data_model.message

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.mgt.zalo.R
import java.util.*

data class FileMessage(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var senderId: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false,
        override var url: String = "unknown",
        override var uploadProgress:Int?=null,
        override var size:Long=-1,
        var fileName: String? = null
) : MediaMessage(id, createdTime, senderId, senderAvatarUrl, TYPE_FILE, isSent, url, uploadProgress, size, null) {
    override fun toMap(): HashMap<String, Any?> {
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