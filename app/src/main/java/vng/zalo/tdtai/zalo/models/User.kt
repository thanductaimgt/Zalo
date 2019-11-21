package vng.zalo.tdtai.zalo.models

import com.google.firebase.Timestamp

data class User(
        var phone: String? = null,// also id
        var avatarUrl: String? = null,
        var birthDate: Timestamp? = null,
        var isMale: Boolean? = false,
        var joinDate: Timestamp? = null,
        var isOnline:Boolean?=false){
    companion object{
        const val FIELD_PHONE = "phone"
        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_BIRTH_DATE_SECS = "birthDateSecs"
        const val FIELD_BIRTH_DATE_NANO_SECS = "birthDateNanoSecs"
        const val FIELD_IS_MALE = "isMale"
        const val FIELD_JOIN_DATE_SECS = "joinDateSecs"
        const val FIELD_JOIN_DATE_NANO_SECS = "joinDateNanoSecs"
        const val FIELD_IS_LOGIN = "isLogin"
        const val FIELD_IS_ONLINE = "isOnline"
    }
}