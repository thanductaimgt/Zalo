package vng.zalo.tdtai.zalo.data_model.media

abstract class Media(
        open var uri:String
){
    companion object{
        const val PAYLOAD_DISPLAY_MORE = 0
    }
}