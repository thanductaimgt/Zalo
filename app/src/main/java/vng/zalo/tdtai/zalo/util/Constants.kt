package vng.zalo.tdtai.zalo.util

import android.graphics.Bitmap
import java.util.*

object Constants {
    const val MAX_UNSEEN_MSG_NUM = 50

    const val ONE_SECOND_IN_MILLISECOND = 1000
    const val ONE_MIN_IN_MILLISECOND = 60 * ONE_SECOND_IN_MILLISECOND
    const val ONE_HOUR_IN_MILLISECOND = 60 * ONE_MIN_IN_MILLISECOND
    const val ONE_DAY_IN_MILLISECOND = 24 * ONE_HOUR_IN_MILLISECOND
    const val SEVEN_DAYS_IN_MILLISECOND = 7 * ONE_DAY_IN_MILLISECOND

    const val ROOM_ID = "ROOM_ID"
    const val ROOM_DISPLAY_NAME = "ROOM_DISPLAY_NAME"
    const val ROOM_NAME = "ROOM_NAME"
    const val ROOM_PHONE = "ROOM_PHONE"
    const val ROOM_AVATAR = "ROOM_AVATAR"

    const val CHOOSE_IMAGE_REQUEST = 0
    const val CHOOSE_VIDEO_REQUEST = 1
    const val CHOOSE_FILE_REQUEST = 2
    const val CAPTURE_IMAGE_REQUEST = 3
    const val CAPTURE_VIDEO_REQUEST = 4

    const val PROVIDER_AUTHORITY = "fileprovider"

    const val FILE_PREFIX = "ZaloFile_"

    const val CHAT_NOTIFY_CHANNEL_ID = "CHAT"
    const val CHAT_NOTIFY_CHANNEL_NAME = "Chat"
    const val UPLOAD_NOTIFY_CHANNEL_ID = "UPLOAD"
    const val UPLOAD_NOTIFY_CHANNEL_NAME = "Upload"

    const val SHOW_KEYBOARD = "SHOW_KEYBOARD"

    const val NOTIFICATION_LIKE_PENDING_INTENT = 1
    const val NOTIFICATION_REPLY_PENDING_INTENT = 2

    const val ACTION_CALL = "vng.zalo.tdtai.zalo.ACTION_CALL"
    const val IS_CALLER = "IS_CALLER"
    const val CALL_TYPE = "CALL_TYPE"

    const val ONE_KB_IN_B: Long = 1000
    const val ONE_MB_IN_B: Long = 1000 * 1000
    const val ONE_GB_IN_B: Long = 1000 * 1000 * 1000

    val IMAGE_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG
    const val IMAGE_COMPRESS_QUALITY = 75

    const val DEFAULT_KEYBOARD_SIZE_DP = 320
    const val DEFAULT_STORY_LIVE_TIME:Long = 365L * ONE_DAY_IN_MILLISECOND

    val TIMESTAMP_EPOCH = Date(0).time

    const val ACTIVITY_NAME = "activity"
    const val FRAGMENT_NAME = "fragment"
    const val SUB_FRAGMENT_NAME = "sub_fragment"
}