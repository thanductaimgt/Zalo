package vng.zalo.tdtai.zalo.models.room

import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.models.RoomMember

data class RoomGroup(
        override var id: String? = null,
        override var avatarUrl: String? = null,
        override var createdTime: Timestamp? = null,
        override var name: String? = null,
        override var memberMap: Map<String, RoomMember>? = null
):Room(id, avatarUrl, createdTime, name, memberMap, TYPE_GROUP)