package vng.zalo.tdtai.zalo.models

import com.airbnb.lottie.LottieComposition

data class Sticker(var id: String? = null,
                   var url: String? = null,
                   var lottieComposition: LottieComposition?=null) {
    fun toMap(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            put("url", url!!)
        }
    }
}