package vng.zalo.tdtai.zalo.model.message

import android.content.Context

data class TypingMessage(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var senderPhone: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false
) :Message(id, createdTime, senderPhone, senderAvatarUrl, TYPE_TYPING, isSent){
    override fun getPreviewContent(context: Context): String {
        return "Writing ..."
    }
}