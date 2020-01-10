package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.models.message.Message
import java.util.*

//mapping firestore data to POJO needs empty constructor
data class RoomItem(var roomId: String? = null,
                    var avatarUrl: String? = null,
                    var lastMsg: String? = null,
                    var lastMsgTime: Timestamp? = null,
//                    var lastMsgType: Int? = Message.TYPE_TEXT,
                    var name: String? = null,
                    var unseenMsgNum: Int = 0,
                    var roomType: Int? = Room.TYPE_PEER,
                    var lastSenderPhone: String? = null,
                    var lastOnlineTime: Timestamp? = Timestamp.now()) {

    fun toMap(): Map<String, Any?> {
        return HashMap<String, Any?>().apply {
            put(FIELD_AVATAR_URL, avatarUrl)
            put(FIELD_LAST_MSG, lastMsg)
            put(FIELD_LAST_MSG_TIME, lastMsgTime)
//            put(FIELD_LAST_MSG_TYPE, lastMsgType)
            put(FIELD_NAME, name)
            put(FIELD_UNSEEN_MSG_NUM, unseenMsgNum)
            put(FIELD_ROOM_TYPE, roomType)
            put(FIELD_LAST_SENDER_PHONE, roomType)
        }
    }

    companion object {
        const val PAYLOAD_NEW_MESSAGE = 0
        const val PAYLOAD_SEEN_MESSAGE = 1
        const val PAYLOAD_ONLINE_STATUS = 2

        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_LAST_MSG = "lastMsg"
        const val FIELD_LAST_MSG_TIME = "lastMsgTime"
//        const val FIELD_LAST_MSG_TYPE = "lastMsgType"
        const val FIELD_NAME = "name"
        const val FIELD_UNSEEN_MSG_NUM = "unseenMsgNum"
        const val FIELD_ROOM_TYPE = "roomType"
        const val FIELD_LAST_SENDER_PHONE = "lastSenderPhone"
    }
}