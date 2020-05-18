package vng.zalo.tdtai.zalo.data_model.media

data class VideoMedia(
        override var uri: String?=null,
        override var ratio: String?=null,
        override var description: String="",
        override var reactCount: Int=0,
        override var commentCount: Int=0,
        override var shareCount: Int=0,
        var duration: Int = 0
) : Media(uri, ratio, description, reactCount, commentCount, shareCount){
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            put(FIELD_DURATION, duration)
        }
    }

    override fun applyMap(map: Map<String, Any?>) {
        super.applyMap(map).apply {
            map[FIELD_DURATION]?.let { duration = (it as Long).toInt() }
        }
    }

    companion object{
        const val FIELD_DURATION = "duration"
    }
}