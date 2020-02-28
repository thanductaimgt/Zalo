package vng.zalo.tdtai.zalo.model

data class Sticker(var id: String? = null,
                   var url: String? = null) {
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            url?.let{put(FIELD_URL, it)}

        }
    }

    companion object{
        const val FIELD_URL = "url"
    }
}