package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.utils.Constants

import java.util.HashMap

//mapping firestore data to POJO needs empty constructor
data class RoomItem(var roomId: String? = null,
                    var avatarUrl: String? = null,
                    var lastMsg: String? = null,
                    var lastMsgTime: Timestamp? = null,
                    var name: String? = null,
                    var unseenMsgNum: Int = 0,
                    var roomType: Int? = Constants.ROOM_TYPE_PEER,
                    var lastSenderPhone: String? = null) {

    fun toMap(): Map<String, Any?> {
        return HashMap<String, Any?>().apply {
            put("avatarUrl", avatarUrl)
            put("lastMsg", lastMsg)
            put("lastMsgTime", lastMsgTime)
            put("name", name)
            put("unseenMsgNum", unseenMsgNum)
            put("roomType", roomType)
        }
    }

    companion object{
        const val PAYLOAD_NEW_MESSAGE = 0
    }
}