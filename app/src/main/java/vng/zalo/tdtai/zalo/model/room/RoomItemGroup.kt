package vng.zalo.tdtai.zalo.model.room

import vng.zalo.tdtai.zalo.model.RoomMember

//mapping firestore data to POJO needs empty constructor
data class RoomItemGroup(override var roomId: String? = null,
                         override var name: String? = null,
                        override var avatarUrl: String? = null,
                        override var lastMsg: String? = null,
                        override var lastMsgTime: Long? = null,
                        override var unseenMsgNum: Int = 0,
                        override var lastSenderPhone: String? = null,
                         override var lastSenderName: String? = null,//todo: change this
                         override var lastTypingMember: RoomMember? = null):RoomItem(
        roomId, name, avatarUrl, lastMsg, lastMsgTime, unseenMsgNum, Room.TYPE_GROUP, lastSenderPhone, lastSenderName, lastTypingMember
)