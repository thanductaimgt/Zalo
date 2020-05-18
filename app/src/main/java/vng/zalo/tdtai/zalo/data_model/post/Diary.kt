package vng.zalo.tdtai.zalo.data_model.post

import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.data_model.media.Media

data class Diary(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var ownerId: String? = null,
        override var ownerName: String? = null,
        override var ownerAvatarUrl: String? = null,
        override var text: String = "",
        override var reactCount: Int = 0,
        override var commentCount: Int = 0,
        override var shareCount: Int = 0,
        var medias: ArrayList<Media> = ArrayList()
) : Post(
        id, createdTime, ownerId, ownerName, ownerAvatarUrl, text, reactCount, commentCount, shareCount, TYPE_DIARY
) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            put(FIELD_MEDIAS, medias.map { it.toMap() })
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        id = doc.id
        doc.get(FIELD_MEDIAS)?.let { medias = (it as ArrayList<Any>).map {any->
            Media.fromObject(any)
        } as ArrayList<Media> }
    }

    fun getFirstMedia(): Media? {
        return medias.firstOrNull()
    }

    companion object {
        const val FIELD_MEDIAS = "medias"

        fun fromDoc(doc: DocumentSnapshot): Diary {
            return Diary().apply { applyDoc(doc) }
        }
    }
}