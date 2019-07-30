package vng.zalo.tdtai.zalo.zalo.models

data class Sticker(var id:String?=null,
                   var url:String?=null){
    fun toMap():Map<String,Any>{
        return HashMap<String,Any>().apply {
            put("url",url!!)
        }
    }
}