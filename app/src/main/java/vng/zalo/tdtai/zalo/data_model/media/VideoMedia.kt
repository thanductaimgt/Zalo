package vng.zalo.tdtai.zalo.data_model.media

data class VideoMedia(
        override var uri: String,
        var duration: Int = 0
) : Media(uri)