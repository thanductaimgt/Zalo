package com.mgt.zalo.data_model.story

import com.google.firebase.firestore.DocumentSnapshot
import com.mgt.zalo.data_model.react.React
import java.util.*
import kotlin.collections.ArrayList

data class ImageStory(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var audioUrl: String? = null,
        override var audioName: String? = null,
        override var duration: Int? = 5,
        override var viewCount: Int = 0,
        override var reactCount: Int = 0,
        override var groupsId: List<String> = ArrayList(),
        override var reacts: HashMap<String, React> = hashMapOf(),
        var imageUrl: String? = null
) : Story(id, createdTime, audioUrl, audioName, duration, TYPE_IMAGE, viewCount, reactCount, groupsId, reacts){
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            imageUrl?.let { put(FIELD_IMAGE_URL, it)}
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_IMAGE_URL)?.let { imageUrl = it }
    }

    companion object {
        const val FIELD_IMAGE_URL = "imageUrl"
    }
}