package vng.zalo.tdtai.zalo.data_model.post

import com.google.firebase.firestore.DocumentSnapshot

data class Diary(
        override var id: String? = null,
        override var createdTime: Long = System.currentTimeMillis(),
        override var ownerId: String? = null,
        override var ownerName: String? = null,
        override var ownerAvatarUrl: String? = null,
        override var text: String = "",
        override var reactCount: Int = 0,
        override var commentCount: Int = 0,
        override var shareNumCount: Int = 0,
        var imagesUrl:List<String> = ArrayList(),
        var videosUrl:List<String> = ArrayList()
) :Post(
        id, createdTime, ownerId, ownerName, ownerAvatarUrl, text, reactCount, commentCount, shareNumCount, TYPE_DIARY
){
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            put(FIELD_IMAGES_URL, imagesUrl)
            put(FIELD_VIDEOS_URL, videosUrl)
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        id = doc.id
        doc.get(FIELD_IMAGES_URL)?.let { imagesUrl = it as List<String> }
        doc.get(FIELD_VIDEOS_URL)?.let { videosUrl = it as List<String> }
    }

    companion object {
        const val FIELD_IMAGES_URL = "imagesUrl"
        const val FIELD_VIDEOS_URL = "videoUrl"
    }
}