package vng.zalo.tdtai.zalo.model.post

import com.google.firebase.firestore.DocumentSnapshot

data class Post(
        open var id: String? = null,
        open var createdTime: Long? = null,
        open var ownerPhone: String? = null,
        open var ownerName: String? = null,
        open var ownerAvatarUrl: String? = null,
//        open var type: Int? = null,
        open var description: String?=null,
        open var imagesUrl:List<String>? = null,
        open var videosUrl:List<String>?=null,
        open var emojiNum:Int?=null,
        open var commentNum:Int?=null,
        open var shareNum:Int?=null
) {
    open fun toMap(): HashMap<String, Any> {
        return HashMap<String, Any>().apply {
            createdTime?.let { put(FIELD_CREATED_TIME, it) }
            ownerPhone?.let { put(FIELD_OWNER_PHONE, it) }
            ownerName?.let { put(FIELD_OWNER_NAME, it) }
            ownerAvatarUrl?.let { put(FIELD_OWNER_AVATAR_URL, it) }
            description?.let { put(FIELD_DESCRIPTION, it) }
            imagesUrl?.let { put(FIELD_IMAGES_URL, it) }
            videosUrl?.let { put(FIELD_VIDEOS_URL, it) }
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

//    abstract fun getPreviewContent(context: Context): String

    protected open fun applyDoc(doc: DocumentSnapshot) {
        id = doc.id
        doc.getLong(FIELD_CREATED_TIME)?.let { createdTime = it }
        doc.getString(FIELD_OWNER_PHONE)?.let { ownerPhone = it }
        doc.getString(FIELD_OWNER_NAME)?.let { ownerName = it }
        doc.getString(FIELD_OWNER_AVATAR_URL)?.let { ownerAvatarUrl = it }
        doc.getString(FIELD_DESCRIPTION)?.let { description = it }
        doc.get(FIELD_IMAGES_URL)?.let { imagesUrl = it as List<String>? }
        doc.get(FIELD_VIDEOS_URL)?.let { videosUrl = it as List<String>? }
    }

    companion object {
//        const val PAYLOAD_TIME = 0
//        const val PAYLOAD_AVATAR = 2
//        const val PAYLOAD_UPLOAD_PROGRESS = 3
//        const val PAYLOAD_SEND_STATUS = 4
//        const val PAYLOAD_SEEN = 5

//        const val TYPE_TEXT = 0
//        const val TYPE_IMAGE = 1
//        const val TYPE_STICKER = 2
//        const val TYPE_FILE = 3
//        const val TYPE_VIDEO = 4
//        const val TYPE_TYPING = 5
//        const val TYPE_CALL = 6
//        const val TYPE_SEEN = 7

        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_OWNER_PHONE = "ownerPhone"
        const val FIELD_OWNER_NAME = "ownerName"
        const val FIELD_OWNER_AVATAR_URL = "ownerAvatarUrl"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_IMAGES_URL = "imagesUrl"
        const val FIELD_VIDEOS_URL = "videoUrl"

//        const val FIELD_TYPE = "type"

        fun fromDoc(doc: DocumentSnapshot): Post {
            return Post().apply { applyDoc(doc)}
        }
    }
}