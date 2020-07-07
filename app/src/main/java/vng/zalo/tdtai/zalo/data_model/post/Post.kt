package vng.zalo.tdtai.zalo.data_model.post

import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.data_model.BaseDataModel
import vng.zalo.tdtai.zalo.data_model.react.React

abstract class Post(
        open var id: String?,
        open var createdTime: Long?,
        open var ownerId: String?,
        open var ownerName: String?,
        open var ownerAvatarUrl: String?,
        open var text: String,
        open var reactCount: Int,
        open var commentCount: Int,
        open var shareCount: Int,
        open var reacts: HashMap<String, React>,
        var type: Int
) : BaseDataModel {
    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            createdTime?.let { put(FIELD_CREATED_TIME, it)}
            ownerId?.let { put(FIELD_OWNER_ID, it) }
            put(FIELD_REACT_COUNT, reactCount)
            put(FIELD_COMMENT_COUNT, commentCount)
            put(FIELD_SHARE_COUNT, shareCount)
            put(FIELD_TEXT, text)
            put(FIELD_REACTS, reacts)
            put(FIELD_TYPE, type)
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        id = doc.id
        doc.getLong(FIELD_CREATED_TIME)?.let { createdTime = it }
        doc.getString(FIELD_OWNER_ID)?.let { ownerId = it }
        doc.getString(FIELD_TEXT)?.let { text = it }
        doc.getLong(FIELD_REACT_COUNT)?.let { reactCount = it.toInt() }
        doc.getLong(FIELD_COMMENT_COUNT)?.let { commentCount = it.toInt() }
        doc.getLong(FIELD_SHARE_COUNT)?.let { shareCount = it.toInt() }
        doc.getLong(FIELD_TYPE)?.let { type = it.toInt() }
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
        const val PAYLOAD_METRICS = 0

        const val TYPE_DIARY = 0
        const val TYPE_WATCH = 1

        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_OWNER_ID = "ownerId"
        const val FIELD_TEXT = "description"
        const val FIELD_REACT_COUNT = "reactCount"
        const val FIELD_COMMENT_COUNT = "commentCount"
        const val FIELD_SHARE_COUNT = "shareCount"
        const val FIELD_TYPE = "type"
        const val FIELD_REACTS = "reacts"

        fun fromDoc(doc: DocumentSnapshot): Post {
            return when (doc.getLong(FIELD_TYPE)?.toInt()) {
                TYPE_DIARY -> Diary()
                else -> Watch()
            }.apply { applyDoc(doc) }
        }
    }
}