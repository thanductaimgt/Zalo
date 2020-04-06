package vng.zalo.tdtai.zalo.data_model.message

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.data_model.BaseDataModel
import java.util.*

abstract class Message(
        open var id: String? = null,
        open var createdTime: Long? = null,
        open var senderId: String? = null,
        open var senderAvatarUrl: String? = null,
        open var type: Int? = null,
        open var isSent: Boolean = false
) : BaseDataModel {
    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            createdTime?.let { put(FIELD_CREATED_TIME, it) }
            senderId?.let { put(FIELD_SENDER_ID, it) }
            type?.let { put(FIELD_TYPE, it) }
        }
    }

    fun clone(): Message {
        return when (type) {
            TYPE_IMAGE -> (this as ImageMessage).copy()
            TYPE_STICKER -> (this as StickerMessage).copy()
            TYPE_FILE -> (this as FileMessage).copy()
            TYPE_VIDEO -> (this as VideoMessage).copy()
            TYPE_TYPING -> (this as TypingMessage).copy()
            TYPE_CALL -> (this as CallMessage).copy()
            else -> (this as TextMessage).copy()
        }
    }

    abstract fun getPreviewContent(context: Context): String

    override fun applyDoc(doc: DocumentSnapshot) {
        id = doc.id
        doc.getLong(FIELD_CREATED_TIME)?.let { createdTime = it }
        doc.getString(FIELD_SENDER_ID)?.let { senderId = it }
        doc.getLong(FIELD_TYPE)?.let { type = it.toInt() }
        isSent = !doc.metadata.hasPendingWrites()
    }

    companion object {
        const val PAYLOAD_TIME = 0
        const val PAYLOAD_AVATAR = 2
        const val PAYLOAD_UPLOAD_PROGRESS = 3
        const val PAYLOAD_SEND_STATUS = 4
        const val PAYLOAD_SEEN = 5

        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_STICKER = 2
        const val TYPE_FILE = 3
        const val TYPE_VIDEO = 4
        const val TYPE_TYPING = 5
        const val TYPE_CALL = 6
        const val TYPE_SEEN = 7

        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_SENDER_ID = "senderId"
        const val FIELD_TYPE = "type"

        fun fromDoc(doc: DocumentSnapshot): Message {
            return when (doc.getLong(FIELD_TYPE)?.toInt()) {
                TYPE_STICKER -> StickerMessage()
                TYPE_IMAGE -> ImageMessage()
                TYPE_FILE -> FileMessage()
                TYPE_CALL -> CallMessage()
                TYPE_VIDEO -> VideoMessage()
                else -> TextMessage()
            }.apply { applyDoc(doc) }
        }
    }
}