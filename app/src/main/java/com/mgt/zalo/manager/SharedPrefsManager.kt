package com.mgt.zalo.manager

import android.content.Context
import android.content.SharedPreferences
import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.data_model.User
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.Utils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsManager @Inject constructor(
        private val application: ZaloApplication,
        private val utils: Utils
) {
    private fun getSharedPrefs(sharePrefsName: String = APP_SHARE_PREFERENCES_NAME): SharedPreferences {
        return application.getSharedPreferences(sharePrefsName, Context.MODE_PRIVATE)
    }

    private fun getSharedPrefsEditor(sharePrefsName: String = APP_SHARE_PREFERENCES_NAME): SharedPreferences.Editor {
        return getSharedPrefs(sharePrefsName).edit()
    }

    fun isLogin(): Boolean {
        return getSharedPrefs().getBoolean(User.FIELD_IS_LOGIN, false)
    }

    fun setIsLogin(isLogin: Boolean) {
        getSharedPrefsEditor()
                .putBoolean(FIELD_IS_LOGIN, isLogin)
                .apply()
    }

    fun getUser(): User {
        getSharedPrefs().apply {
            return User(
                    id = getString(User.FIELD_ID, null),
                    name = getString(User.FIELD_NAME, null),
                    avatarUrl = getString(User.FIELD_AVATAR_URL, null),
                    birthDate = getLong(User.FIELD_BIRTH_DATE, 0),
                    isMale = getBoolean(User.FIELD_IS_MALE, true),
                    joinDate = getLong(User.FIELD_JOIN_DATE, 0)
            )
        }
    }

    fun setUser(user: User) {
        getSharedPrefsEditor()
                .putString(User.FIELD_ID, user.id)
                .putString(User.FIELD_NAME, user.name)
                .putString(User.FIELD_AVATAR_URL, user.avatarUrl)
                .putLong(User.FIELD_BIRTH_DATE, user.birthDate!!)
                .putBoolean(User.FIELD_IS_MALE, user.isMale!!)
                .putLong(User.FIELD_JOIN_DATE, user.joinDate!!)
                .putBoolean(User.FIELD_IS_LOGIN, true)
                .apply()
    }

    fun getGroupSortType(): Int {
        return getSharedPrefs().getInt(FIELD_GROUP_SORT_TYPE, 0)
    }

    fun setGroupSortType(position: Int) {
        getSharedPrefsEditor()
                .putInt(FIELD_GROUP_SORT_TYPE, position)
                .apply()
    }

    fun getVideoThumbCacheUri(videoUri: String): String? {
        return getSharedPrefs(VIDEO_THUMB_SHARE_PREFERENCES_NAME).getString(videoUri, null)
    }

    fun setVideoThumbCacheUri(videoUri: String, thumbUri: String) {
        getSharedPrefsEditor(VIDEO_THUMB_SHARE_PREFERENCES_NAME)
                .putString(videoUri, thumbUri)
                .apply()
    }

    fun getKeyboardSize(): Int {
        return getSharedPrefs().getInt(FIELD_KEYBOARD_SIZE, utils.dpToPx(Constants.DEFAULT_KEYBOARD_SIZE_DP))
    }

    fun setKeyboardSize(sizeInPx: Int) {
        getSharedPrefsEditor()
                .putInt(FIELD_KEYBOARD_SIZE, sizeInPx)
                .apply()
    }

    fun getFirebaseMessagingToken(): String? {
        return getSharedPrefs().getString(FIREBASE_MESSAGING_TOKEN, null)
    }

    fun setFirebaseMessagingToken(token: String) {
        getSharedPrefsEditor().apply {
            putString(FIREBASE_MESSAGING_TOKEN, token)
            apply()
        }
    }

    fun getCameraSide(): Boolean {
        return getSharedPrefs().getBoolean(IS_CAMERA_FRONT, true)
    }

    fun setCameraSide(isFront: Boolean) {
        getSharedPrefsEditor()
                .putBoolean(IS_CAMERA_FRONT, isFront)
                .apply()
    }

    fun isWatchTabFullScreen(): Boolean {
        return getSharedPrefs().getBoolean(IS_WATCH_TAB_FULLSCREEN, false)
    }

    fun setWatchTabFullScreen(isFull: Boolean) {
        getSharedPrefsEditor()
                .putBoolean(IS_WATCH_TAB_FULLSCREEN, isFull)
                .apply()
    }

    fun isNotificationChannelsInit():Boolean{
        return getSharedPrefs().getBoolean(IS_NOTIFICATION_CHANNELS_INIT, false)
    }

    fun setNotificationChannelsInit(isInit:Boolean){
        getSharedPrefsEditor()
                .putBoolean(IS_NOTIFICATION_CHANNELS_INIT, isInit)
                .apply()
    }

    companion object {
        private const val APP_SHARE_PREFERENCES_NAME = "AppSharePreferences"
        private const val VIDEO_THUMB_SHARE_PREFERENCES_NAME = "VideoThumbSharePreferences"

        private const val FIELD_IS_LOGIN = "isLogin"
        private const val FIELD_GROUP_SORT_TYPE = "groupSortType"
        private const val FIELD_KEYBOARD_SIZE = "keyboardSize"

        private const val FIREBASE_MESSAGING_TOKEN = "FIREBASE_MESSAGING_TOKEN"
        private const val IS_CAMERA_FRONT = "IS_CAMERA_FRONT"
        private const val IS_WATCH_TAB_FULLSCREEN = "IS_WATCH_TAB_FULLSCREEN"
        private const val IS_NOTIFICATION_CHANNELS_INIT = "IS_NOTIFICATION_CHANNELS_INIT"
    }
}