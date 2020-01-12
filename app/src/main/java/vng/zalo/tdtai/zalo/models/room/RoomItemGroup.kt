package vng.zalo.tdtai.zalo.models.room

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*
import kotlin.collections.HashMap

//mapping firestore data to POJO needs empty constructor
data class RoomItemGroup(override var roomId: String? = null,
                         override var name: String? = null,
                        override var avatarUrl: String? = null,
                        override var lastMsg: String? = null,
                        override var lastMsgTime: Timestamp? = null,
                        override var unseenMsgNum: Int = 0,
                        override var lastSenderPhone: String? = null,
                         override var lastSenderName: String? = null,//todo: change this
                         override var lastTypingPhone: String? = null):RoomItem(
        roomId, name, avatarUrl, lastMsg, lastMsgTime, unseenMsgNum, Room.TYPE_GROUP, lastSenderPhone, lastSenderName, lastTypingPhone
)