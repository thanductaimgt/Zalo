package vng.zalo.tdtai.zalo.zalo.models

import com.google.firebase.Timestamp

data class Room(var id: String? = null,
                var avatar: String? = null,
                var createdTime: Timestamp? = null,
                var name: String? = null,
                var memberMap: Map<String, RoomMember>? = null)