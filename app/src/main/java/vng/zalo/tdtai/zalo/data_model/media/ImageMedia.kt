package vng.zalo.tdtai.zalo.data_model.media

data class ImageMedia(
        override var uri: String?=null,
        override var ratio: String? = null,
        override var description: String="",
        override var reactCount: Int=0,
        override var commentCount: Int=0,
        override var shareCount: Int=0
) : Media(uri, ratio, description, reactCount, commentCount, shareCount)