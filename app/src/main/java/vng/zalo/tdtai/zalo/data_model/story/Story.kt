package vng.zalo.tdtai.zalo.data_model.story

import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.data_model.BaseDataModel
import vng.zalo.tdtai.zalo.data_model.react.React
import java.util.*
import kotlin.collections.ArrayList

abstract class Story(
        open var id: String? = null,
        open var createdTime: Long? = null,
        open var audioUrl: String? = null,
        open var audioName: String? = null,
        open var duration: Int? = 5,
        open var type: Int? = null,
        open var viewCount: Int = 0,
        open var reactCount: Int = 0,
        open var groupsId: List<String> = ArrayList(),
        open var reacts: HashMap<String, React>
) : BaseDataModel {
    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            createdTime?.let { put(FIELD_CREATED_TIME, it) }
            audioUrl?.let { put(FIELD_AUDIO_URL, it) }
            audioName?.let { put(FIELD_AUDIO_NAME, it) }
            duration?.let { put(FIELD_DURATION, it) }
            type?.let { put(FIELD_TYPE, it) }
            put(FIELD_VIEW_COUNT, viewCount)
            put(FIELD_REACT_COUNT, reactCount)
            put(FIELD_GROUPS_ID, groupsId)
            put(FIELD_REACTS, reacts)
        }
    }

//    fun clone(): Story {
//        return when (type) {
//            TYPE_IMAGE -> (this as ImageStory).copy()
//            else -> (this as VideoStory).copy()
//        }
//    }

    override fun applyDoc(doc: DocumentSnapshot) {
        id = doc.id
        doc.getLong(FIELD_CREATED_TIME)?.let { createdTime = it }
        doc.getString(FIELD_AUDIO_URL)?.let { audioUrl = it }
        doc.getString(FIELD_AUDIO_NAME)?.let { audioName = it }
        doc.getLong(FIELD_DURATION)?.let { duration = it.toInt() }
        doc.getLong(FIELD_TYPE)?.let { type = it.toInt() }
        doc.getLong(FIELD_VIEW_COUNT)?.let { viewCount = it.toInt() }
        doc.getLong(FIELD_REACT_COUNT)?.let { reactCount = it.toInt() }
        doc.get(FIELD_GROUPS_ID)?.let { groupsId = it as List<String> }
        doc.get(FIELD_REACTS)?.let {
            reacts = (it as HashMap<String, Long?>).mapValues { entry->
                React(
                        ownerId = entry.key,
                        type = if (entry.value != null) entry.value!!.toInt() else React.TYPE_LOVE
                )
            } as HashMap<String, React>
        }
    }

    companion object {
//        const val PAYLOAD_TIME = 0
//        const val PAYLOAD_AVATAR = 2
//        const val PAYLOAD_UPLOAD_PROGRESS = 3
//        const val PAYLOAD_SEND_STATUS = 4
//        const val PAYLOAD_SEEN = 5

        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO = 1

        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_AUDIO_URL = "audioUrl"
        const val FIELD_AUDIO_NAME = "audioName"
        const val FIELD_DURATION = "duration"
        const val FIELD_TYPE = "type"
        const val FIELD_VIEW_COUNT = "viewCount"
        const val FIELD_REACT_COUNT = "reactCount"
        const val FIELD_GROUPS_ID = "groupsId"
        const val FIELD_REACTS = "reacts"

        fun fromDoc(doc: DocumentSnapshot): Story {
            return when (doc.getLong(FIELD_TYPE)?.toInt()) {
                TYPE_IMAGE -> ImageStory()
                else -> VideoStory()
            }.apply { applyDoc(doc) }
        }
    }
}