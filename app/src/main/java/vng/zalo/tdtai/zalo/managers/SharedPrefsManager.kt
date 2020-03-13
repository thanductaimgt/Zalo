package vng.zalo.tdtai.zalo.managers

import android.content.Context
import android.content.SharedPreferences
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.model.User
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils
import javax.inject.Inject

interface SharedPrefsManager {
    fun isLogin(): Boolean

    fun setIsLogin(isLogin: Boolean)

    fun getUser(): User

    fun setUser(user: User)

    fun getGroupSortType(): Int

    fun setGroupSortType(position: Int)

    fun getVideoThumbCacheUri(videoUri: String): String?

    fun setVideoThumbCacheUri(videoUri: String, thumbUri: String)

    fun getKeyboardSize(): Int

    fun setKeyboardSize(sizeInPx: Int)

    fun getFirebaseMessagingToken():String?

    fun setFirebaseMessagingToken(token:String)
}

class SharedPrefsManagerImpl @Inject constructor(
        private val application: ZaloApplication,
        private val utils: Utils
) : SharedPrefsManager {
    private fun getSharedPrefs(sharePrefsName: String = APP_SHARE_PREFERENCES_NAME): SharedPreferences {
        return application.getSharedPreferences(sharePrefsName, Context.MODE_PRIVATE)
    }

    private fun getSharedPrefsEditor(sharePrefsName: String = APP_SHARE_PREFERENCES_NAME): SharedPreferences.Editor {
        return getSharedPrefs(sharePrefsName).edit()
    }

    override fun isLogin(): Boolean {
        return getSharedPrefs().getBoolean(User.FIELD_IS_LOGIN, false)
    }

    override fun setIsLogin(isLogin: Boolean) {
        val editor = getSharedPrefsEditor()
        editor.putBoolean(FIELD_IS_LOGIN, isLogin)
        editor.apply()
    }

    override fun getUser(): User {
        val prefs = getSharedPrefs()
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

    override fun setUser(user: User) {
        getSharedPrefsEditor().apply {
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

    override fun getGroupSortType(): Int {
        return getSharedPrefs().getInt(FIELD_GROUP_SORT_TYPE, 0)
    }

    override fun setGroupSortType(position: Int) {
        getSharedPrefsEditor().apply {
            putInt(FIELD_GROUP_SORT_TYPE, position)
            apply()
        }
    }

    override fun getVideoThumbCacheUri(videoUri: String): String? {
        val prefs = getSharedPrefs(VIDEO_THUMB_SHARE_PREFERENCES_NAME)
        return prefs.getString(videoUri, null)
    }

    override fun setVideoThumbCacheUri(videoUri: String, thumbUri: String) {
        val editor = getSharedPrefsEditor(VIDEO_THUMB_SHARE_PREFERENCES_NAME)
        editor.putString(videoUri, thumbUri)
        editor.apply()
    }

    override fun getKeyboardSize(): Int {
        val prefs = getSharedPrefs()
        return prefs.getInt(FIELD_KEYBOARD_SIZE, utils.dpToPx(Constants.DEFAULT_KEYBOARD_SIZE_DP).toInt())
    }

    override fun setKeyboardSize(sizeInPx: Int) {
        val editor = getSharedPrefsEditor()
        editor.putInt(FIELD_KEYBOARD_SIZE, sizeInPx)
        editor.apply()
    }

    override fun getFirebaseMessagingToken(): String? {
        return getSharedPrefs().getString(FIREBASE_MESSAGING_TOKEN, null)
    }

    override fun setFirebaseMessagingToken(token: String) {
        getSharedPrefsEditor().apply {
            putString(FIREBASE_MESSAGING_TOKEN, token)
            apply()
        }
    }

    companion object{
        private const val APP_SHARE_PREFERENCES_NAME = "AppSharePreferences"
        private const val VIDEO_THUMB_SHARE_PREFERENCES_NAME = "VideoThumbSharePreferences"

        private const val FIELD_IS_LOGIN = "isLogin"
        private const val FIELD_GROUP_SORT_TYPE = "groupSortType"
        private const val FIELD_KEYBOARD_SIZE = "keyboardSize"

        private const val FIREBASE_MESSAGING_TOKEN = "FIREBASE_MESSAGING_TOKEN"
    }
}