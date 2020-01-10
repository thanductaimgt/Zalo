package vng.zalo.tdtai.zalo.utils

import android.Manifest
import com.google.firebase.Timestamp
import java.util.*

object Constants {
    const val MAX_UNSEEN_MSG_NUM = 50

    const val ONE_SECOND_IN_MILLISECOND = 1000
    const val ONE_MIN_IN_MILLISECOND = 60 * ONE_SECOND_IN_MILLISECOND
    const val ONE_HOUR_IN_MILLISECOND = 60 * ONE_MIN_IN_MILLISECOND
    const val ONE_DAY_IN_MILLISECOND = 24 * ONE_HOUR_IN_MILLISECOND
    const val SEVEN_DAYS_IN_MILLISECOND = 7 * ONE_DAY_IN_MILLISECOND

    const val ROOM_ID = "ROOM_ID"
    const val ROOM_NAME = "ROOM_NAME"
    const val ROOM_AVATAR = "ROOM_AVATAR"
    const val ROOM_TYPE = "ROOM_TYPE"

    const val COLLECTION_USERS = "users"
    const val COLLECTION_ROOMS = "rooms"
    const val COLLECTION_MESSAGES = "messages"
    const val COLLECTION_MEMBERS = "members"
    const val COLLECTION_CONTACTS = "contacts"
    const val COLLECTION_OFFICIAL_ACCOUNTS = "official_accounts"
    const val COLLECTION_STICKER_SETS = "sticker_sets"
    const val COLLECTION_STICKERS = "stickers"

    const val CHOOSE_IMAGES_REQUEST = 0
    const val CHOOSE_FILES_REQUEST = 1
    const val TAKE_PICTURE_REQUEST = 2

    const val FOLDER_USER_AVATARS = "user_avatars"
    const val FOLDER_ROOM_AVATARS = "room_avatars"
    const val FOLDER_ROOM_DATA = "room_data"
    const val FOLDER_STICKER_SETS = "sticker_sets"

    const val PROVIDER_AUTHORITY = "fileprovider"

    const val FILE_PREFIX = "ZaloFile_"

    const val CHAT_NOTIFY_CHANNEL_ID = "0"
    const val CHAT_NOTIFY_CHANNEL_NAME = "Chat Notification Channel"

    const val SHOW_KEYBOARD = "SHOW_KEYBOARD"

    const val NOTIFICATION_PENDING_INTENT = 0
    const val NOTIFICATION_REPLY_PENDING_INTENT = 1

    const val SIP_DOMAIN = "sip.linphone.org"
    const val SIP_ACCOUNT_PREFIX = "zalo.vng.tdtai.zalo.user."

    const val ACTION_CALL = "vng.zalo.tdtai.zalo.ACTION_CALL"
    const val IS_CALLER = "IS_CALLER"
    const val CALL_TYPE = "CALL_TYPE"

    const val ONE_KB_IN_B:Long = 1000
    const val ONE_MB_IN_B:Long = 1000*1000
    const val ONE_GB_IN_B:Long = 1000*1000*1000

    val TIMESTAMP_EPOCH = Timestamp(Date(0))
}