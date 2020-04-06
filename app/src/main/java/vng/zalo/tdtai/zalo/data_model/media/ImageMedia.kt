package vng.zalo.tdtai.zalo.data_model.media

data class ImageMedia(
        override var uri: String,
        var ratio: String? = null
) : Media(uri)