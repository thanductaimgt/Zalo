package vng.zalo.tdtai.zalo.models

data class StickerSet(var name: String? = null,
                      var bucketName: String? = null,//also id
                      var stickers: List<Sticker>? = null) {
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            put("name", name!!)
            put("bucketName", bucketName!!)
        }
    }
}