package com.mgt.zalo.data_model.message

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import java.util.HashMap

data class TextMessage(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var senderId: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false,
        var content: String? = null
) :Message(id, createdTime, senderId, senderAvatarUrl, TYPE_TEXT, isSent){
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            content?.let{put(FIELD_CONTENT, it)}
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_CONTENT)?.let { content = it }
    }

    override fun getPreviewContent(context: Context): String {
        return content!!
    }

    companion object {
        const val FIELD_CONTENT = "content"
    }
}