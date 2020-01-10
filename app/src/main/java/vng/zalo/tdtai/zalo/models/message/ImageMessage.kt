package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.R
import java.util.*

data class ImageMessage(
        override var id: String? = null,
        override var createdTime: Timestamp? = null,
        override var senderPhone: String? = null,
        override var senderAvatarUrl: String? = null,
        override var type: Int? = null,
        var url: String? = null,
        var ratio: String? = null
) : Message(id, createdTime, senderPhone, senderAvatarUrl, type) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            put(FIELD_URL, url)
            put(FIELD_RATIO, ratio)
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_URL)?.let { url = it }
        doc.getString(FIELD_RATIO)?.let { ratio = it }
    }

    override fun getPreviewContent(context: Context): String {
        return "[${context.getString(R.string.description_image)}]"
    }

    companion object {
        const val FIELD_URL = "url"
        const val FIELD_RATIO = "ratio"
    }
}