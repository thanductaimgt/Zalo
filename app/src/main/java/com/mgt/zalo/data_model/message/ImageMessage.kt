package com.mgt.zalo.data_model.message

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.mgt.zalo.R
import java.util.*

data class ImageMessage(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var senderId: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false,
        override var url: String = "unknown",
        override var uploadProgress:Int?=null,
        override var size:Long=-1,
        override var ratio: String? = null
) : MediaMessage(id, createdTime, senderId, senderAvatarUrl, TYPE_IMAGE, isSent, url, uploadProgress, size, ratio) {
    override fun toMap(): HashMap<String, Any?> {
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