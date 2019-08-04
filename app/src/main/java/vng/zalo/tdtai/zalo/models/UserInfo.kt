package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp

data class UserInfo(
        var phone: String? = null,// also id
        var avatarUrl: String? = null,
        var birthDate: Timestamp? = null,
        var isMale: Boolean? = false,
        var joinDate: Timestamp? = null)