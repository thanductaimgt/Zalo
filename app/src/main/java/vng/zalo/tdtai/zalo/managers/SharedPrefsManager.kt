package vng.zalo.tdtai.zalo.managers

import android.content.Context
import android.content.SharedPreferences
import vng.zalo.tdtai.zalo.model.User
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils
import javax.inject.Inject
import javax.inject.Singleton

interface SharedPrefsManager {
    fun isLogin(context: Context): Boolean

    fun setIsLogin(context: Context, isLogin: Boolean)

    fun getUser(context: Context): User

    fun setUser(context: Context, user: User)

    fun getGroupSortType(context: Context): Int

    fun setGroupSortType(context: Context, position: Int)

    fun getVideoThumbCacheUri(context: Context, videoUri: String): String?

    fun setVideoThumbCacheUri(context: Context, videoUri: String, thumbUri: String)

    fun getKeyboardSize(context: Context): Int

    fun setKeyboardSize(context: Context, sizeInPx: Int)
}

class SharedPrefsManagerImpl @Inject constructor(
        private val utils: Utils
) : SharedPrefsManager {
    private fun getSharedPrefs(context: Context, sharePrefsName: String = APP_SHARE_PREFERENCES_NAME): SharedPreferences {
        return context.getSharedPreferences(sharePrefsName, Context.MODE_PRIVATE)
    }

    private fun getSharedPrefsEditor(context: Context, sharePrefsName: String = APP_SHARE_PREFERENCES_NAME): SharedPreferences.Editor {
        return getSharedPrefs(context, sharePrefsName).edit()
    }

    override fun isLogin(context: Context): Boolean {
        return getSharedPrefs(context).getBoolean(User.FIELD_IS_LOGIN, false)
    }

    override fun setIsLogin(context: Context, isLogin: Boolean) {
        val editor = getSharedPrefsEditor(context)
        editor.putBoolean(FIELD_IS_LOGIN, isLogin)
        editor.apply()
    }

    override fun getUser(context: Context): User {
        val prefs = getSharedPrefs(context)
        prefs.apply {
            return User(
                    phone = getString(User.FIELD_PHONE, null),
                    name = getString(User.FIELD_NAME, null),
                    avatarUrl = getString(User.FIELD_AVATAR_URL, null),
                    birthDate = getLong(User.FIELD_BIRTH_DATE, 0),
                    isMale = getBoolean(User.FIELD_IS_MALE, true),
                    joinDate = getLong(User.FIELD_JOIN_DATE, 0)
            )
        }
    }

    override fun setUser(context: Context, user: User) {
        getSharedPrefsEditor(context).apply {
            putString(User.FIELD_PHONE, user.phone)
            putString(User.FIELD_NAME, user.name)
            putString(User.FIELD_AVATAR_URL, user.avatarUrl)
            putLong(User.FIELD_BIRTH_DATE, user.birthDate!!)
            putBoolean(User.FIELD_IS_MALE, user.isMale!!)
            putLong(User.FIELD_JOIN_DATE, user.joinDate!!)
            putBoolean(User.FIELD_IS_LOGIN, true)

            apply()
        }
    }

    override fun getGroupSortType(context: Context): Int {
        return getSharedPrefs(context).getInt(FIELD_GROUP_SORT_TYPE, 0)
    }

    override fun setGroupSortType(context: Context, position: Int) {
        getSharedPrefsEditor(context).apply {
            putInt(FIELD_GROUP_SORT_TYPE, position)
            apply()
        }
    }

    override fun getVideoThumbCacheUri(context: Context, videoUri: String): String? {
        val prefs = getSharedPrefs(context, VIDEO_THUMB_SHARE_PREFERENCES_NAME)
        return prefs.getString(videoUri, null)
    }

    override fun setVideoThumbCacheUri(context: Context, videoUri: String, thumbUri: String) {
        val editor = getSharedPrefsEditor(context, VIDEO_THUMB_SHARE_PREFERENCES_NAME)
        editor.putString(videoUri, thumbUri)
        editor.apply()
    }

    override fun getKeyboardSize(context: Context): Int {
        val prefs = getSharedPrefs(context)
        return prefs.getInt(FIELD_KEYBOARD_SIZE, utils.dpToPx(context, Constants.DEFAULT_KEYBOARD_SIZE_DP).toInt())
    }

    override fun setKeyboardSize(context: Context, sizeInPx: Int) {
        val editor = getSharedPrefsEditor(context)
        editor.putInt(FIELD_KEYBOARD_SIZE, sizeInPx)
        editor.apply()
    }

    companion object{
        private const val APP_SHARE_PREFERENCES_NAME = "AppSharePreferences"
        private const val VIDEO_THUMB_SHARE_PREFERENCES_NAME = "VideoThumbSharePreferences"

        private const val FIELD_IS_LOGIN = "isLogin"
        private const val FIELD_GROUP_SORT_TYPE = "groupSortType"
        private const val FIELD_KEYBOARD_SIZE = "keyboardSize"
    }
}