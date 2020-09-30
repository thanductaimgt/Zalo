package com.mgt.zalo.data_model.room

import com.google.firebase.firestore.DocumentSnapshot
import com.mgt.zalo.data_model.RoomMember

//mapping firestore data to POJO needs empty constructor
data class RoomItemPeer(override var roomId: String? = null,
                        override var name: String? = null,
                        override var avatarUrl: String? = null,
                        override var lastMsg: String? = null,
                        override var lastMsgTime: Long? = null,
                        override var unseenMsgNum: Int = 0,
                        override var lastSenderId: String? = null,
                        override var lastSenderName: String? = null,
                        override var lastTypingMember: RoomMember? = null,
                        var peerId: String? = null,
                        var lastOnlineTime: Long? = System.currentTimeMillis()) : RoomItem(
        roomId, name, avatarUrl, lastMsg, lastMsgTime, unseenMsgNum, Room.TYPE_PEER, lastSenderId, lastSenderName, lastTypingMember
) {

    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            peerId?.let { put(FIELD_PEER_ID, it) }
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_PEER_ID)?.let { peerId = it }
        doc.getLong(FIELD_LAST_ONLINE_TIME)?.let { lastOnlineTime = it }
    }

    companion object {
        const val FIELD_PEER_ID = "peerId"
        const val FIELD_LAST_ONLINE_TIME = "lastOnlineTime"
    }
}