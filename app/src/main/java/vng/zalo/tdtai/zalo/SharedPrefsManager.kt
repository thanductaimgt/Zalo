package vng.zalo.tdtai.zalo

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.models.User

object SharedPrefsManager{
    private fun getSharedPrefs(context: Context):SharedPreferences{
        return context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private fun getSharedPrefsEditor(context: Context):SharedPreferences.Editor{
        return getSharedPrefs(context).edit()
    }

    fun isLogin(context: Context):Boolean{
        return getSharedPrefs(context).getBoolean(User.FIELD_IS_LOGIN, false)
    }

    fun setIsLogin(context: Context, isLogin:Boolean){
        val editor = getSharedPrefsEditor(context)
        editor.putBoolean(FIELD_IS_LOGIN, isLogin)
        editor.apply()
    }

    fun getUser(context: Context):User{
        val prefs = getSharedPrefs(context)
        prefs.apply {
            return User(
                    phone = getString(User.FIELD_PHONE, null),
                    name = getString(User.FIELD_NAME, null),
                    avatarUrl = getString(User.FIELD_AVATAR_URL, null),
                    birthDate = Timestamp(getLong(User.FIELD_BIRTH_DATE_SECS, 0), getInt(User.FIELD_BIRTH_DATE_NANO_SECS, 0)),
                    isMale = getBoolean(User.FIELD_IS_MALE, true),
                    joinDate = Timestamp(getLong(User.FIELD_JOIN_DATE_SECS, 0), getInt(User.FIELD_JOIN_DATE_NANO_SECS, 0))
            )
        }
    }

    fun setUser(context: Context, user: User) {
        getSharedPrefsEditor(context).apply {
            putString(User.FIELD_PHONE, user.phone)
            putString(User.FIELD_NAME, user.name)
            putString(User.FIELD_AVATAR_URL, user.avatarUrl)

            putLong(User.FIELD_BIRTH_DATE_SECS, user.birthDate!!.seconds)
            putInt(User.FIELD_BIRTH_DATE_NANO_SECS, user.birthDate!!.nanoseconds)

            putBoolean(User.FIELD_IS_MALE, user.isMale!!)

            putLong(User.FIELD_JOIN_DATE_SECS, user.joinDate!!.seconds)
            putInt(User.FIELD_JOIN_DATE_NANO_SECS, user.joinDate!!.nanoseconds)

            putBoolean(User.FIELD_IS_LOGIN, true)

            apply()
        }
    }

    fun getGroupSortType(context: Context):Int{
        return getSharedPrefs(context).getInt(FIELD_GROUP_SORT_TYPE, 0)
    }

    fun setGroupSortType(context: Context, position:Int){
        getSharedPrefsEditor(context).apply {
            putInt(FIELD_GROUP_SORT_TYPE, position)
            apply()
        }
    }

    private const val SHARE_PREFERENCES_NAME = "ZaloSharePreferences"

    private const val FIELD_IS_LOGIN = "isLogin"
    private const val FIELD_GROUP_SORT_TYPE = "groupSortType"
}