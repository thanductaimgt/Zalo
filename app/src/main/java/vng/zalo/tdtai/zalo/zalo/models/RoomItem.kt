package vng.zalo.tdtai.zalo.zalo.models

import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.zalo.utils.Constants

import java.util.HashMap

//mapping firestore data to POJO needs empty constructor
data class RoomItem(var roomId: String? = null,
                    var avatar: String? = null,
                    var lastMsg: String? = null,
                    var lastMsgTime: Timestamp? = null,
                    var name: String? = null,
                    var unseenMsgNum: Int = 0,
                    var roomType: Int? = Constants.ROOM_TYPE_PEER,
                    var lastSenderPhone: String? = null) {

    fun toMap(): Map<String, Any?> {
        return HashMap<String, Any?>().apply {
            put("avatar", avatar)
            put("lastMsg", lastMsg)
            put("lastMsgTime", lastMsgTime)
            put("name", name)
            put("unseenMsgNum", unseenMsgNum)
            put("roomType", roomType)
        }
    }
}