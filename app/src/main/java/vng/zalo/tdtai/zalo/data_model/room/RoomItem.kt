package vng.zalo.tdtai.zalo.data_model.room

import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.data_model.BaseDataModel
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.data_model.RoomMember
import java.util.*

//mapping firestore data to POJO needs empty constructor
abstract class RoomItem(open var roomId: String? = null,
                        open var name: String? = null,
                        open var avatarUrl: String? = null,
                        open var lastMsg: String? = null,
                        open var lastMsgTime: Long? = null,
//                  open   var lastMsgType: Int? = Message.TYPE_TEXT,
                        open var unseenMsgNum: Int = 0,
                        open var roomType: Int,
                        open var lastSenderId: String? = null,
                        open var lastSenderName: String? = null,//todo: change this
                        open var lastTypingMember: RoomMember? = null): BaseDataModel {

    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            name?.let { put(FIELD_NAME, it) }
            avatarUrl?.let { put(FIELD_AVATAR_URL, it) }
            lastMsg?.let { put(FIELD_LAST_MSG, it) }
            put(FIELD_LAST_MSG_TIME, lastMsgTime)
//            lastMsgType?.let{put(FIELD_LAST_MSG_TYPE, it)}
            put(FIELD_UNSEEN_MSG_NUM, unseenMsgNum)
            put(FIELD_ROOM_TYPE, roomType)
            lastSenderId?.let { put(FIELD_LAST_SENDER_ID, it) }
            lastSenderName?.let { put(FIELD_LAST_SENDER_NAME, it) }
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        roomId = doc.id
        doc.getString(FIELD_NAME)?.let { name = it }
        doc.getString(FIELD_AVATAR_URL)?.let { avatarUrl = it }
        doc.getString(FIELD_LAST_MSG)?.let { lastMsg = it }
        doc.getLong(FIELD_LAST_MSG_TIME)?.let { lastMsgTime = it }
        doc.getLong(FIELD_UNSEEN_MSG_NUM)?.let { unseenMsgNum = it.toInt() }
        doc.getLong(FIELD_ROOM_TYPE)?.let { roomType = it.toInt() }
        doc.getString(FIELD_LAST_SENDER_ID)?.let { lastSenderId = it }
        doc.getString(FIELD_LAST_SENDER_NAME)?.let { lastSenderName = it }
    }

    fun getDisplayName(resourceManager: ResourceManager): String {
        return if (this is RoomItemPeer) {
            resourceManager.getNameFromId(this.peerId!!) ?: name!!
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
        const val FIELD_LAST_SENDER_ID = "lastSenderId"
        const val FIELD_LAST_SENDER_NAME = "lastSenderName"

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