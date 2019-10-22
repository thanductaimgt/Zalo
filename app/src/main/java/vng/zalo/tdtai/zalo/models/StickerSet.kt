package vng.zalo.tdtai.zalo.models

data class StickerSet(var name: String? = null,
                      var bucketName: String? = null,//also id
                      var stickers: List<Sticker>? = null) {
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            put(FIELD_NAME, name!!)
            put(FIELD_BUCKET_NAME, bucketName!!)
        }
    }

    companion object{
        const val FIELD_NAME = "name"
        const val FIELD_BUCKET_NAME = "bucketName"
    }
}