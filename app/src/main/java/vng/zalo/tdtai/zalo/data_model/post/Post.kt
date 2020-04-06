package vng.zalo.tdtai.zalo.data_model.post

import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.data_model.BaseDataModel

abstract class Post(
        open var id: String?,
        open var createdTime: Long,
        open var ownerId: String?,
        open var ownerName: String?,
        open var ownerAvatarUrl: String?,
        open var text: String,
        open var reactCount: Int,
        open var commentCount: Int,
        open var shareNumCount: Int,
        var type: Int
) : BaseDataModel {
    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            put(FIELD_CREATED_TIME, createdTime)
            ownerId?.let { put(FIELD_OWNER_ID, it) }
            put(FIELD_REACT_COUNT, reactCount)
            put(FIELD_COMMENT_COUNT, commentCount)
            put(FIELD_SHARE_COUNT, shareNumCount)
            put(FIELD_TEXT, text)
            put(FIELD_TYPE, type)
        }
    }

//    fun clone():Message{
//        return when(type){
//            TYPE_IMAGE->(this as ImageMessage).copy()
//            TYPE_STICKER->(this as StickerMessage).copy()
//            TYPE_FILE->(this as FileMessage).copy()
//            TYPE_VIDEO->(this as VideoMessage).copy()
//            TYPE_TYPING->(this as TypingMessage).copy()
//            TYPE_CALL->(this as CallMessage).copy()
//            else->(this as TextMessage).copy()
//        }
//    }

    override fun applyDoc(doc: DocumentSnapshot) {
        id = doc.id
        doc.getLong(FIELD_CREATED_TIME)?.let { createdTime = it }
        doc.getString(FIELD_OWNER_ID)?.let { ownerId = it }
        doc.getString(FIELD_TEXT)?.let { text = it }
        doc.getLong(FIELD_REACT_COUNT)?.let { reactCount = it.toInt() }
        doc.getLong(FIELD_COMMENT_COUNT)?.let { commentCount = it.toInt() }
        doc.getLong(FIELD_SHARE_COUNT)?.let { shareNumCount = it.toInt() }
        doc.getLong(FIELD_TYPE)?.let { type = it.toInt() }
    }

    companion object {
//        const val PAYLOAD_TIME = 0
//        const val PAYLOAD_AVATAR = 2
//        const val PAYLOAD_UPLOAD_PROGRESS = 3
//        const val PAYLOAD_SEND_STATUS = 4
//        const val PAYLOAD_SEEN = 5

        const val TYPE_DIARY = 0
        const val TYPE_WATCH = 1

        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_OWNER_ID = "ownerId"
        const val FIELD_TEXT = "description"
        const val FIELD_REACT_COUNT = "reactCount"
        const val FIELD_COMMENT_COUNT = "commentCount"
        const val FIELD_SHARE_COUNT = "shareCount"
        const val FIELD_TYPE = "type"

        fun fromDoc(doc: DocumentSnapshot): Post {
            return when (doc.getLong(FIELD_TYPE)?.toInt()) {
                TYPE_DIARY -> Diary()
                else -> Watch()
            }.apply { applyDoc(doc) }
        }
    }
}