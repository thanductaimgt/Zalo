package vng.zalo.tdtai.zalo.data_model.message

import android.content.Context

data class TypingMessage(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var senderId: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false
) :Message(id, createdTime, senderId, senderAvatarUrl, TYPE_TYPING, isSent){
    override fun getPreviewContent(context: Context): String {
        return "Writing ..."
    }
}