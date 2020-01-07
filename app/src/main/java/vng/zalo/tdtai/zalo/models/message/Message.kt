package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.R
import java.util.*

open class Message(
        var id:String?=null,
        var content: String? = null,// url if message is a sticker or image
        var createdTime: Timestamp? = null,
        var senderPhone: String? = null,
        var senderAvatarUrl: String? = null,
        var type: Int? = null,
        var ratio: String? = null,
        var fileSize: Long? = null,// for file only
        var fileName: String? = null// for file only
) {
    open fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            content?.let { put(FIELD_CONTENT, it) }
            createdTime?.let { put(FIELD_CREATED_TIME, it) }
            senderPhone?.let { put(FIELD_SENDER_PHONE, it) }
            type?.let { put(FIELD_TYPE, it) }
            ratio?.let { put(FIELD_RATIO, it) }
            this@Message.fileSize?.let { put(FIELD_FILE_SIZE, it) }
            fileName?.let { put(FIELD_FILE_NAME, it) }
        }
    }

    open fun getPreviewContent(context: Context): String {
        return when (type) {
            TYPE_TEXT -> content!!
            TYPE_STICKER -> "[Sticker]"
            TYPE_IMAGE -> "[${context.getString(R.string.description_image)}]"
            TYPE_FILE -> "[File] $fileName"
            else -> ""
        }
    }

    open fun applyDoc(doc:DocumentSnapshot){
        id = doc.id
        doc.getString(FIELD_CONTENT)?.let{content = it}
        doc.getTimestamp(FIELD_CREATED_TIME)?.let{createdTime = it}
        doc.getString(FIELD_SENDER_PHONE)?.let{senderPhone = it}
        doc.getLong(FIELD_TYPE)?.let{type = it.toInt()}
        doc.getString(FIELD_RATIO)?.let{ratio = it}
        doc.getLong(FIELD_FILE_SIZE)?.let{fileSize = it}
        doc.getString(FIELD_FILE_NAME)?.let{fileName = it}
    }

    companion object {
        const val PAYLOAD_TIME = 0
        const val PAYLOAD_AVATAR = 2

        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_STICKER = 2
        const val TYPE_FILE = 3
        const val TYPE_VIDEO = 4
        const val TYPE_TYPING = 5
        const val TYPE_CALL = 6

        const val FIELD_CONTENT = "content"
        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_SENDER_PHONE = "senderPhone"
        const val FIELD_TYPE = "type"
        const val FIELD_RATIO = "ratio"
        const val FIELD_FILE_SIZE = "fileSize"
        const val FIELD_FILE_NAME = "fileName"
    }
}