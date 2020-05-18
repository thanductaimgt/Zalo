package vng.zalo.tdtai.zalo.data_model.media

abstract class Media(
        open var uri: String?,
        open var ratio: String?,
        open var description: String,
        open var reactCount: Int,
        open var commentCount: Int,
        open var shareCount: Int
) {
    open fun toMap(): HashMap<String, Any?> {
        return hashMapOf<String, Any?>().apply {
            put(FIELD_URL, uri)
            put(FIELD_RATIO, ratio)
            put(FIELD_TYPE, if (this@Media is ImageMedia) TYPE_IMAGE else TYPE_VIDEO)
            put(FIELD_RATIO, ratio)
            put(FIELD_DESCRIPTION, description)
            put(FIELD_REACT_COUNT, reactCount)
            put(FIELD_COMMENT_COUNT, commentCount)
            put(FIELD_SHARE_COUNT, shareCount)
        }
    }

    open fun applyMap(map: Map<String, Any?>) {
        uri = map[FIELD_URL] as String
        ratio = map[FIELD_RATIO] as String
        map[FIELD_DESCRIPTION]?.let { description = it as String }
        map[FIELD_REACT_COUNT]?.let { reactCount = (it as Long).toInt() }
        map[FIELD_COMMENT_COUNT]?.let { commentCount = (it as Long).toInt() }
        map[FIELD_SHARE_COUNT]?.let { shareCount = (it as Long).toInt() }
    }

    companion object {
        const val PAYLOAD_DISPLAY_MORE = 0
        const val PAYLOAD_RATIO = 2
        const val PAYLOAD_PREVIEW = 1

        const val FIELD_TYPE = "type"
        const val FIELD_URL = "url"
        const val FIELD_RATIO = "ratio"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_REACT_COUNT = "reactCount"
        const val FIELD_COMMENT_COUNT = "commentCount"
        const val FIELD_SHARE_COUNT = "shareCount"

        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO = 1

        fun fromObject(any: Any): Media {
            any as Map<String, Any?>

            val type = (any[FIELD_TYPE] as Long).toInt()
            return when (type) {
                TYPE_IMAGE -> ImageMedia()
                else -> VideoMedia()
            }.apply { applyMap(any) }
        }
    }
}