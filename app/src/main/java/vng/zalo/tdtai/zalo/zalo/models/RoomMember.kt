package vng.zalo.tdtai.zalo.zalo.models

import com.google.firebase.Timestamp

data class RoomMember(
        var phone: String? = null,
        var avatar: String? = null,
        var joinDate: Timestamp? = null
)