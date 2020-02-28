package vng.zalo.tdtai.zalo.model.room

import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.model.RoomMember

data class RoomPeer(
        override var id: String? = null,
        override var avatarUrl: String? = null,
        override var createdTime: Long? = null,
        override var name: String? = null,
        override var memberMap: Map<String, RoomMember>? = null,
        var phone:String?=null
):Room(id, avatarUrl, createdTime, name, memberMap, TYPE_PEER) {
    override fun toMap(): HashMap<String, Any> {
        return super.toMap().apply {
            phone?.let { put(FIELD_PHONE, it) }
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_PHONE)?.let { phone = it }
    }

    companion object {
        const val FIELD_PHONE = "phone"
    }
}