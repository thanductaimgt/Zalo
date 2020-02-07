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
        override var isSent: Boolean=false,
        override var url: String = "unknown",
        override var uploadProgress:Int?=null,
        override var size:Long=-1,
        var ratio: String? = null
) : ResourceMessage(id, createdTime, senderPhone, senderAvatarUrl, TYPE_IMAGE, isSent, url, uploadProgress, size) {
    override fun toMap(): HashMap<String, Any> {
        return super.toMap().apply {
            ratio?.let { put(FIELD_RATIO, it)}
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_RATIO)?.let { ratio = it }
    }

    override fun getPreviewContent(context: Context): String {
        return "[${context.getString(R.string.description_image)}]"
    }

    companion object {
        const val FIELD_RATIO = "ratio"
    }
}