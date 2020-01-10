package vng.zalo.tdtai.zalo.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.views.activities.RoomActivity


class NotificationService : Service() {
    private lateinit var binder: ServiceBinder
    private lateinit var target: Target
    private var lastRoomItems: HashMap<String, RoomItem>? = null
    private val listenerRegistrations = ArrayList<ListenerRegistration>()
    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        binder = ServiceBinder()

        initNotificationChannels()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand.intent: $intent")
        //logout case
        listenerRegistrations.forEach { it.remove() }

        startListenForNewMessages()

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        listenerRegistrations.forEach { it.remove() }
    }

    private fun initNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChatNotificationChannel()
        }
    }

    private fun startListenForNewMessages() {
        val userRoomsListener = Database.addUserRoomsListener(
                userPhone = ZaloApplication.curUser!!.phone!!,
                fieldToOrder = RoomItem.FIELD_LAST_MSG_TIME,
                orderDirection = Query.Direction.DESCENDING
        ) { roomItems ->
            //            val pushedNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                notificationManager.activeNotifications.toList()
//            } else {
//                ArrayList()
//            }

            lastRoomItems?.let { lastRoomItems ->
                roomItems.filter {
                    val lastRoomItem = lastRoomItems[it.roomId]

                    lastRoomItem != null &&
                            lastRoomItem.unseenMsgNum != it.unseenMsgNum &&
                            it.unseenMsgNum > 0 &&
                            it.roomId != ZaloApplication.currentRoomId
                }.forEach {
                    pushChatNotification(it)
                }
            }

            val map = HashMap<String, RoomItem>()
            roomItems.forEach {
                map[it.roomId!!] = it
            }
            lastRoomItems = map
        }
        listenerRegistrations.add(userRoomsListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initChatNotificationChannel() {
        val chan = NotificationChannel(
                Constants.CHAT_NOTIFY_CHANNEL_ID,
                Constants.CHAT_NOTIFY_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = getColor(R.color.lightPrimary)
        chan.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
    }

    private fun pushChatNotification(roomItem: RoomItem) {
        target = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                placeHolderDrawable?.let { pushChatNotification(it.toBitmap(), roomItem) }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
                pushChatNotification(bitmap, roomItem)
            }
        }

        Picasso.get().load(roomItem.avatarUrl).placeholder(
                if (roomItem.roomType == Room.TYPE_PEER)
                    R.drawable.default_peer_avatar
                else
                    R.drawable.default_group_avatar
        ).into(target)
    }

    private fun pushChatNotification(roomAvatarBitmap: Bitmap, roomItem: RoomItem) {
        // create the pending intent and add to the notification
        val notificationIntent = Intent(this, RoomActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            putExtra(Constants.ROOM_NAME, roomItem.name)
            putExtra(Constants.ROOM_AVATAR, roomItem.avatarUrl)
            putExtra(Constants.ROOM_ID, roomItem.roomId)
            putExtra(Constants.ROOM_TYPE, roomItem.roomType)
        }
        val notificationPendingIntent = PendingIntent.getActivity(
                this,
                Utils.getNotificationIdFromRoomId(roomItem.roomId!!),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val roomPerson = Person.Builder()
                .setKey(roomItem.lastSenderPhone)
                .setName(roomItem.lastSenderPhone)
                .setIcon(IconCompat.createWithBitmap(roomAvatarBitmap))
                .build()

        val lastSenderPerson = Person.Builder()
                .setKey(roomItem.lastSenderPhone)
                .setName(roomItem.lastSenderPhone)
                .build()

        val notificationStyle = NotificationCompat.MessagingStyle(roomPerson)
                .let {
                    if (roomItem.roomType == Room.TYPE_PEER)
                        it.setGroupConversation(false)
                    else
                        it.setGroupConversation(true).setConversationTitle("${roomItem.name}")
                }
                .addMessage(roomItem.lastMsg, roomItem.lastMsgTime!!.toDate().time, lastSenderPerson)

        val replyIntent = notificationIntent.apply { putExtra(Constants.SHOW_KEYBOARD, true) }

        val notification = NotificationCompat.Builder(this, Constants.CHAT_NOTIFY_CHANNEL_ID)
                .setColor(ContextCompat.getColor(this, R.color.lightPrimary))
                .setContentIntent(notificationPendingIntent)
                .setSmallIcon(R.drawable.app_icon_transparent)
                .setLargeIcon(roomAvatarBitmap)
                .setStyle(notificationStyle)
                .addAction(R.drawable.like, getString(R.string.label_like), null)
                .addAction(R.drawable.like, getString(R.string.label_reply),
                        PendingIntent.getActivity(this, roomItem.roomId.hashCode().shl(Constants.NOTIFICATION_REPLY_PENDING_INTENT), replyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(this).notify(roomItem.roomId.hashCode(), notification)
    }

    inner class ServiceBinder : Binder() {
        fun getService(): NotificationService {
            return this@NotificationService
        }
    }
}
