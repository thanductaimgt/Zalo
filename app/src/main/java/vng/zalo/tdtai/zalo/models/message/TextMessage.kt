package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.HashMap

data class TextMessage(
        override var id: String? = null,
        override var createdTime: Timestamp? = null,
        override var senderPhone: String? = null,
        override var senderAvatarUrl: String? = null,
        override var type: Int? = null,
        var content: String? = null
) :Message(id, createdTime, senderPhone, senderAvatarUrl, type){
    override fun toMap(): HashMap<String, Any> {
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