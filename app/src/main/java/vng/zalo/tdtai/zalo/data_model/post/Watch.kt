package vng.zalo.tdtai.zalo.data_model.post

import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.data_model.react.React
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia

data class Watch(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var ownerId: String? = null,
        override var ownerName: String? = null,
        override var ownerAvatarUrl: String? = null,
        override var text: String = "",
        override var reactCount: Int = 0,
        override var commentCount: Int = 0,
        override var shareCount: Int = 0,
        override var reacts: HashMap<String, React> = hashMapOf(),
        var videoMedia: VideoMedia?=null
//        var musicName:String?=null,
//        var musicUrl:String?=null,
//        var musicOwnerAvatarUrl:String?=null
//        var ratio:String?=null
): Post(
    id, createdTime, ownerId, ownerName, ownerAvatarUrl, text, reactCount, commentCount, shareCount, reacts, TYPE_WATCH
) {
//    override fun toMap(): HashMap<String, Any?> {
//        return super.toMap().apply {
//            videoMedia?.let { put(FIELD_VIDEO_MEDIA, it.toMap()) }
//        }
//    }

    companion object {
        const val PAYLOAD_FULLSCREEN = 0
//        const val PAYLOAD_TIME = 0
//        const val PAYLOAD_AVATAR = 2
//        const val PAYLOAD_UPLOAD_PROGRESS = 3
//        const val PAYLOAD_SEND_STATUS = 4
//        const val PAYLOAD_SEEN = 5

        const val FIELD_VIDEO_MEDIA = "videoMedia"

        // 'post' doc
        fun fromDoc(doc: DocumentSnapshot): ArrayList<Watch> {
            val diary = Diary.fromDoc(doc)
            return diary.medias.filterIsInstance<VideoMedia>().mapIndexed { index, media ->
                Watch().apply {
                    id = "${diary.id}$index"
                    createdTime = diary.createdTime
                    ownerId = diary.ownerId
                    ownerName = diary.ownerName
                    ownerAvatarUrl = diary.ownerAvatarUrl
                    text = diary.text
                    reactCount = diary.reactCount
                    commentCount = diary.commentCount
                    shareCount = diary.shareCount
                    videoMedia = media
                }
            } as ArrayList
        }
    }
}