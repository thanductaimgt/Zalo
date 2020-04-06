package vng.zalo.tdtai.zalo.data_model

import com.google.firebase.firestore.DocumentSnapshot

data class Sticker(var id: String? = null,
                   var url: String? = null):BaseDataModel {
    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            url?.let{put(FIELD_URL, it)}
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        id = doc.id
        doc.getString(FIELD_URL)?.let { url = it }
    }

    companion object{
        const val FIELD_URL = "url"

        fun fromDoc(doc: DocumentSnapshot): Sticker {
            return Sticker().apply { applyDoc(doc) }
        }
    }
}