package vng.zalo.tdtai.zalo.models.room

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

//mapping firestore data to POJO needs empty constructor
data class RoomItemPeer(override var roomId: String? = null,
                        override var name: String? = null,
                        override var avatarUrl: String? = null,
                        override var lastMsg: String? = null,
                        override var lastMsgTime: Timestamp? = null,
                        override var unseenMsgNum: Int = 0,
                        override var lastSenderPhone: String? = null,
                        override var lastSenderName: String? = null,
                        override var lastTypingPhone: String? = null,
                        var phone: String? = null,
                        var lastOnlineTime: Timestamp? = Timestamp.now()) : RoomItem(
        roomId, name, avatarUrl, lastMsg, lastMsgTime, unseenMsgNum, Room.TYPE_PEER, lastSenderPhone, lastSenderName, lastTypingPhone
) {

    override fun toMap(): HashMap<String, Any> {
        return super.toMap().apply {
            phone?.let { put(FIELD_PHONE, it) }
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_PHONE)?.let { phone = it }
        doc.getTimestamp(FIELD_LAST_ONLINE_TIME)?.let { lastOnlineTime = it }
    }

    companion object {
        const val FIELD_PHONE = "phone"
        const val FIELD_LAST_ONLINE_TIME = "lastOnlineTime"
    }
}