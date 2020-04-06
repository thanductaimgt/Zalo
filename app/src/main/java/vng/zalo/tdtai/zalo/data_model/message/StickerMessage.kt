package vng.zalo.tdtai.zalo.data_model.message

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

data class StickerMessage(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var senderId: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false,
        var url: String? = null
) : Message(id, createdTime, senderId, senderAvatarUrl, TYPE_STICKER, isSent) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            url?.let { put(FIELD_URL, it) }
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_URL)?.let { url = it }
    }

    override fun getPreviewContent(context: Context): String {
        return "[Sticker]"
    }

    companion object {
        const val FIELD_URL = "url"
    }
}