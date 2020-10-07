package com.mgt.zalo.data_model.room

import com.google.firebase.firestore.DocumentSnapshot
import com.mgt.zalo.data_model.BaseDataModel
import com.mgt.zalo.data_model.RoomMember
import com.mgt.zalo.manager.ResourceManager

abstract class Room(
        open var id: String? = null,
        open var avatarUrl: String? = null,
        open var createdTime: Long? = null,
        open var name: String? = null,
        open var memberMap: Map<String, RoomMember>? = null,
        open var type: Int = TYPE_PEER): BaseDataModel {
    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            createdTime?.let { put(FIELD_CREATED_TIME, it) }
            put(FIELD_TYPE, type)
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        id = doc.id
        doc.getLong(FIELD_CREATED_TIME)?.let { createdTime = it }
        doc.getLong(FIELD_TYPE)?.let { type = it.toInt() }
    }

    fun getDisplayName(resourceManager: ResourceManager): String {
        return if (this is RoomPeer) {
            resourceManager.getNameFromId(this.peerId!!) ?: name!!
        } else {
            name!!
        }
    }

    companion object {
        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_TYPE = "type"
        const val FIELD_TYPING_MEMBERS = "typingMembers"
        const val FIELD_SEEN_MEMBERS_ID = "seenMembersId"

        const val TYPE_PEER = 0
        const val TYPE_GROUP = 1
    }
}