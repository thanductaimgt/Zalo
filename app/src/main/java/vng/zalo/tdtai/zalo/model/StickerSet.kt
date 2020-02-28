package vng.zalo.tdtai.zalo.model

data class StickerSet(var name: String? = null,
                      var bucketName: String? = null,//also id
                      var stickers: List<Sticker>? = null) {
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            name?.let{put(FIELD_NAME, it)}
            bucketName?.let{put(FIELD_BUCKET_NAME, it)}
        }
    }

    companion object{
        const val FIELD_NAME = "name"
        const val FIELD_BUCKET_NAME = "bucketName"
    }
}