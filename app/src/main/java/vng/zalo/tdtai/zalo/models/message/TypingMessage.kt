package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp

data class TypingMessage(
        override var id: String? = null,
        override var createdTime: Timestamp? = null,
        override var senderPhone: String? = null,
        override var senderAvatarUrl: String? = null,
        override var type: Int? = null
) :Message(id, createdTime, senderPhone, senderAvatarUrl, type){
    override fun getPreviewContent(context: Context): String {
        return "Writing ..."
    }
}