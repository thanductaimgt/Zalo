package vng.zalo.tdtai.zalo.data_model.room

import vng.zalo.tdtai.zalo.data_model.RoomMember

data class RoomGroup(
        override var id: String? = null,
        override var avatarUrl: String? = null,
        override var createdTime: Long? = null,
        override var name: String? = null,
        override var memberMap: Map<String, RoomMember>? = null
):Room(id, avatarUrl, createdTime, name, memberMap, TYPE_GROUP)