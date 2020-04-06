package vng.zalo.tdtai.zalo.data_model.post

import com.google.firebase.firestore.DocumentSnapshot

data class Watch(
        override var id: String? = null,
        override var createdTime: Long = System.currentTimeMillis(),
        override var ownerId: String? = null,
        override var ownerName: String? = null,
        override var ownerAvatarUrl: String? = null,
        override var text: String = "",
        override var reactCount: Int = 0,
        override var commentCount: Int = 0,
        override var shareNumCount: Int = 0,
        var videoUrl:String?=null,
        var duration:Int?=null
//        var musicName:String?=null,
//        var musicUrl:String?=null,
//        var musicOwnerAvatarUrl:String?=null
//        var ratio:String?=null
): Post(
    id, createdTime, ownerId, ownerName, ownerAvatarUrl, text, reactCount, commentCount, shareNumCount, TYPE_WATCH
) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            videoUrl?.let { put(FIELD_VIDEO_URL, it) }
            duration?.let { put(FIELD_DURATION, it) }
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

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_VIDEO_URL)?.let { videoUrl = it}
        doc.getLong(FIELD_DURATION)?.let { duration = it.toInt()}
    }

    companion object {
        const val PAYLOAD_FULLSCREEN = 0
//        const val PAYLOAD_TIME = 0
//        const val PAYLOAD_AVATAR = 2
//        const val PAYLOAD_UPLOAD_PROGRESS = 3
//        const val PAYLOAD_SEND_STATUS = 4
//        const val PAYLOAD_SEEN = 5

        const val FIELD_VIDEO_URL = "videoUrl"
        const val FIELD_DURATION = "duration"
    }
}