package vng.zalo.tdtai.zalo.models.room

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.abstracts.ResourceManager
import vng.zalo.tdtai.zalo.models.RoomMember
import java.util.*

//mapping firestore data to POJO needs empty constructor
abstract class RoomItem(open var roomId: String? = null,
                        open var name: String? = null,
                        open var avatarUrl: String? = null,
                        open var lastMsg: String? = null,
                        open var lastMsgTime: Timestamp? = null,
//                  open   var lastMsgType: Int? = Message.TYPE_TEXT,
                        open var unseenMsgNum: Int = 0,
                        open var roomType: Int,
                        open var lastSenderPhone: String? = null,
                        open var lastSenderName: String? = null,//todo: change this
                        open var lastTypingMember: RoomMember? = null) {

    open fun toMap(): HashMap<String, Any> {
        return HashMap<String, Any>().apply {
            name?.let { put(FIELD_NAME, it) }
            avatarUrl?.let { put(FIELD_AVATAR_URL, it) }
            lastMsg?.let { put(FIELD_LAST_MSG, it) }
            lastMsgTime?.let { put(FIELD_LAST_MSG_TIME, it) }
//            lastMsgType?.let{put(FIELD_LAST_MSG_TYPE, it)}
            put(FIELD_UNSEEN_MSG_NUM, unseenMsgNum)
            put(FIELD_ROOM_TYPE, roomType)
            lastSenderPhone?.let { put(FIELD_LAST_SENDER_PHONE, it) }
            lastSenderName?.let { put(FIELD_LAST_SENDER_NAME, it) }
        }
    }

    protected open fun applyDoc(doc: DocumentSnapshot) {
        roomId = doc.id
        doc.getString(FIELD_NAME)?.let { name = it }
        doc.getString(FIELD_AVATAR_URL)?.let { avatarUrl = it }
        doc.getString(FIELD_LAST_MSG)?.let { lastMsg = it }
        doc.getTimestamp(FIELD_LAST_MSG_TIME)?.let { lastMsgTime = it }
        doc.getLong(FIELD_UNSEEN_MSG_NUM)?.let { unseenMsgNum = it.toInt() }
        doc.getLong(FIELD_ROOM_TYPE)?.let { roomType = it.toInt() }
        doc.getString(FIELD_LAST_SENDER_PHONE)?.let { lastSenderPhone = it }
        doc.getString(FIELD_LAST_SENDER_NAME)?.let { lastSenderName = it }
//        doc.getString(FIELD_LAST_TYPING_PHONE)?.let { lastTypingMember = it }
    }

    fun getDisplayName(): String {
        return if (this is RoomItemPeer) {
            ResourceManager.getNameFromPhone(this.phone!!) ?: name!!
        } else {
            name!!
        }
    }

    companion object {
        const val PAYLOAD_NEW_MESSAGE = 0
        const val PAYLOAD_SEEN_MESSAGE = 1
        const val PAYLOAD_ONLINE_STATUS = 2
        const val PAYLOAD_TYPING = 3
        const val PAYLOAD_SELECT = 4

        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_LAST_MSG = "lastMsg"
        const val FIELD_LAST_MSG_TIME = "lastMsgTime"
        //        const val FIELD_LAST_MSG_TYPE = "lastMsgType"
        const val FIELD_NAME = "name"
        const val FIELD_UNSEEN_MSG_NUM = "unseenMsgNum"
        const val FIELD_ROOM_TYPE = "roomType"
        const val FIELD_LAST_SENDER_PHONE = "lastSenderPhone"
        const val FIELD_LAST_SENDER_NAME = "lastSenderName"
        const val FIELD_LAST_TYPING_PHONE = "lastTypingPhone"

        fun fromDoc(doc: DocumentSnapshot): RoomItem {
            val roomType = doc.getLong(FIELD_ROOM_TYPE)!!.toInt()

            return if (roomType == Room.TYPE_PEER) {
                RoomItemPeer()
            } else {
                RoomItemGroup()
            }.apply { applyDoc(doc) }
                    //todo(remove this)
//                    .also { it.avatarUrl = "https://i.pinimg.com/474x/16/55/cd/1655cd86623d22c6ef4cc45d667544db.jpg" }
        }
    }
}