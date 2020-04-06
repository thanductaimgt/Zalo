package vng.zalo.tdtai.zalo.data_model.room

import com.google.firebase.firestore.DocumentSnapshot
import vng.zalo.tdtai.zalo.data_model.RoomMember

data class RoomPeer(
        override var id: String? = null,
        override var avatarUrl: String? = null,
        override var createdTime: Long? = null,
        override var name: String? = null,
        override var memberMap: Map<String, RoomMember>? = null,
        var peerId:String?=null
):Room(id, avatarUrl, createdTime, name, memberMap, TYPE_PEER) {
    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            peerId?.let { put(FIELD_PEER_ID, it) }
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        doc.getString(FIELD_PEER_ID)?.let { peerId = it }
    }

    companion object {
        const val FIELD_PEER_ID = "peerId"
    }
}