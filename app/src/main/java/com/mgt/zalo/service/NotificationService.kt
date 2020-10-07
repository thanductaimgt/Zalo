package com.mgt.zalo.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.set
import com.google.firebase.firestore.ListenerRegistration
import com.mgt.zalo.R
import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.data_model.ChatNotification
import com.mgt.zalo.data_model.room.Room
import com.mgt.zalo.data_model.room.RoomItem
import com.mgt.zalo.data_model.room.RoomItemPeer
import com.mgt.zalo.manager.ResourceManager
import com.mgt.zalo.manager.SessionManager
import com.mgt.zalo.repository.Database
import com.mgt.zalo.ui.chat.ChatActivity
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.smartLoad
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import javax.inject.Inject


interface NotificationService {
    fun start()

    fun stop()

    fun getNotificationId(roomId: String): Int
}

class AlwaysRunningNotificationService @Inject constructor(
        private val application: ZaloApplication,
        private val database: Database,
        private val sessionManager: SessionManager,
        private val resourceManager: ResourceManager
) : NotificationService {
    private var lastRoomItemMap: HashMap<String, RoomItem>? = null
    private val listenerRegistrations = ArrayList<ListenerRegistration>()
    private var doListening = false
    private val notificationMap = SparseArray<ChatNotification>()

    private var listeningThread: Thread? = null

    override fun start() {
        Log.d(TAG, "onLogin")

        if (listeningThread != null) {
            Toast.makeText(application, "Already started", Toast.LENGTH_SHORT).show()
            return
        }

        doListening = true

        listeningThread = getNewListeningThreadInstance()
        listeningThread!!.start()
        Toast.makeText(application, "Started", Toast.LENGTH_SHORT).show()
    }

    override fun stop() {
        Log.d(TAG, "onLogout")

        listenerRegistrations.forEach { it.remove() }

        if (!doListening) {
            Toast.makeText(application, "Already stopped", Toast.LENGTH_SHORT).show()
            return
        }
        doListening = false
        val obj = object {}
        synchronized(obj) {}
        Toast.makeText(application, "Stopped", Toast.LENGTH_SHORT).show()
    }

    private fun getNewListeningThreadInstance(): Thread {
        return Thread {
            Log.d(TAG, "thread started")

            startListenForNewMessages()

            val obj = object {}
            while (doListening) {
                keepAlive()
                Thread.sleep(3000)

                synchronized(obj) {}
            }

            Log.d(TAG, "thread stopped")

            listeningThread = null
            synchronized(obj) {}
        }
    }

    private fun keepAlive() {
        Handler(Looper.getMainLooper()).post {
            val toast = Toast(application)

            toast.view = View(application).apply {
                visibility = View.GONE
            }
            toast.duration = Toast.LENGTH_LONG
//            val toast = Toast.makeText(context, "${Random().nextInt()}", Toast.LENGTH_LONG)

            toast.show()
        }
    }

    private fun startListenForNewMessages() {
        val userRoomsListener = database.addUserRoomsListener(
                userId = sessionManager.curUser!!.id!!
        ) { roomItems ->
            lastRoomItemMap?.let { lastRoomItemMap ->
                roomItems.filter {
                    val lastRoomItem = lastRoomItemMap[it.roomId]

                    lastRoomItem != null &&
                            lastRoomItem.unseenMsgNum != it.unseenMsgNum &&
                            it.unseenMsgNum > 0 &&
                            it.roomId != sessionManager.currentRoomId
                }.forEach {
                    pushChatNotification(it)
                }
            }

            val map = HashMap<String, RoomItem>()
            roomItems.forEach {
                map[it.roomId!!] = it
            }
            lastRoomItemMap = map
        }
        listenerRegistrations.add(userRoomsListener)
    }

    private fun pushChatNotification(roomItem: RoomItem) {
        val notificationId = getNotificationId(roomItem.roomId!!)

        var notification = notificationMap[notificationId]

        var isPrepareDone = true

        if (notification == null) {
            notification = ChatNotification(notificationId)
            notificationMap[notificationId] = notification

            isPrepareDone = false
        }

        if (notification.bitmapMap[roomItem.roomId!!] == null) {
            isPrepareDone = false

            if (notification.roomAvatarTarget == null) {
                notification.roomAvatarTarget = RoomAvatarTarget(roomItem)

                Picasso.get().smartLoad(roomItem.avatarUrl, resourceManager, notification.roomAvatarTarget!!) {
                    it.placeholder(
                            if (roomItem.roomType == Room.TYPE_PEER)
                                R.drawable.default_peer_avatar
                            else
                                R.drawable.default_group_avatar
                    )
                }
            }
        }

        if (notification.bitmapMap[roomItem.lastSenderId] == null) {
            isPrepareDone = false

            if (notification.senderAvatarTargets[roomItem.lastSenderId] == null) {
                notification.senderAvatarTargets[roomItem.lastSenderId!!] = SenderAvatarTarget(roomItem)

                database.getUserAvatarUrl(roomItem.lastSenderId!!) { avatarUrl ->
                    Picasso.get().smartLoad(avatarUrl, resourceManager, notification.senderAvatarTargets[roomItem.lastSenderId!!]!!) {
                        it.placeholder(R.drawable.default_peer_avatar)
                    }
                }
            }
        }

        if (isPrepareDone) {
            pushChatNotificationInternal(roomItem)
        }
    }

    private fun pushChatNotificationInternal(roomItem: RoomItem) {
        val notificationId = getNotificationId(roomItem.roomId!!)
        val chatNotification = notificationMap[notificationId]

        // create the pending intent and add to the notification
        val notificationIntent = Intent(application, ChatActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            putExtra(Constants.ROOM_NAME, roomItem.name)
            putExtra(Constants.ROOM_AVATAR, roomItem.avatarUrl)
            putExtra(Constants.ROOM_ID, roomItem.roomId)

            if (roomItem is RoomItemPeer) {
                putExtra(Constants.ROOM_PHONE, roomItem.peerId)
            }
        }
        val notificationPendingIntent = PendingIntent.getActivity(
                application,
                notificationId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val roomPerson = Person.Builder()
                .setKey(roomItem.roomId)
                .setName(roomItem.getDisplayName(resourceManager))
                .setIcon(IconCompat.createWithBitmap(chatNotification.bitmapMap[roomItem.roomId!!]))
                .build()

        val lastSenderPerson = Person.Builder()
                .setKey(roomItem.lastSenderId)
                .setName(resourceManager.getNameFromId(roomItem.lastSenderId!!)
                        ?: roomItem.lastSenderName)
                .setIcon(IconCompat.createWithBitmap(chatNotification.bitmapMap[roomItem.lastSenderId!!]))
                .build()

        if (!chatNotification.containsMessage(roomItem.lastMsgTime!!)) {
            chatNotification.addMessage(ChatNotification.ChatNotificationMessage(
                    createdTime = roomItem.lastMsgTime!!,
                    content = roomItem.lastMsg!!,
                    senderPerson = lastSenderPerson
            ))
        }

        val notificationStyle = NotificationCompat.MessagingStyle(roomPerson)
                .let {
                    if (roomItem.roomType == Room.TYPE_PEER)
                        it.setGroupConversation(false)
                    else
                        it.setGroupConversation(true).setConversationTitle(roomItem.getDisplayName(resourceManager))
                }.also { messagingStyle ->
                    chatNotification.lastMessages.forEach {
                        messagingStyle.addMessage(it.content, it.createdTime, it.senderPerson)
                    }
                }

        val replyIntent = notificationIntent.apply { putExtra(Constants.SHOW_KEYBOARD, true) }

        val notification = NotificationCompat.Builder(application, Constants.CHAT_NOTIFY_CHANNEL_ID)
                .setColor(ContextCompat.getColor(application, R.color.lightPrimary))
                .setContentIntent(notificationPendingIntent)
                .setSmallIcon(R.drawable.app_icon_transparent)
//                .setLargeIcon(roomAvatarBitmap)
                .setStyle(notificationStyle)
                .addAction(R.drawable.like, application.getString(R.string.label_like), null)
                .addAction(R.drawable.reply, application.getString(R.string.label_reply),
                        PendingIntent.getActivity(application, getNotificationActionRequestCode(notificationId), replyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(application).notify(notificationId, notification)
    }

    inner class RoomAvatarTarget(private val roomItem: RoomItem) : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
//            placeHolderDrawable?.let {
//                val notification = notificationMap[getNotificationId(roomItem.roomId!!)]
//                notification.roomAvatarBitmap = it.toBitmap()
//
//                if(notification.lastSenderAvatarBitmap!=null){
//                    pushChatNotification(roomItem)
//
//                    notification.lastSenderAvatarBitmap = null
//                    notification.roomAvatarBitmap = null
//                }
//            }
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            errorDrawable?.let {
                checkPushChatNotification(it.toBitmap())
            }
        }

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
            checkPushChatNotification(bitmap)
        }

        private fun checkPushChatNotification(bitmap: Bitmap) {
            val notification = notificationMap[getNotificationId(roomItem.roomId!!)]
            notification.bitmapMap[roomItem.roomId!!] = bitmap

            if (notification.bitmapMap[roomItem.lastSenderId] != null) {
                pushChatNotificationInternal(roomItem)
            }

            notification.roomAvatarTarget = null
        }
    }

    inner class SenderAvatarTarget(private val roomItem: RoomItem) : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
//            placeHolderDrawable?.let {
//                val notification = notificationMap[getNotificationId(roomItem.roomId!!)]
//                notification.lastSenderAvatarBitmap = placeHolderDrawable.toBitmap()
//
//                if(notification.roomAvatarBitmap!=null){
//                    pushChatNotification(roomItem)
//
//                    notification.lastSenderAvatarBitmap = null
//                    notification.roomAvatarBitmap = null
//                }
//            }
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            errorDrawable?.let {
                checkPushChatNotification(it.toBitmap())
            }
        }

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
            checkPushChatNotification(bitmap)
        }

        private fun checkPushChatNotification(bitmap: Bitmap) {
            val notification = notificationMap[getNotificationId(roomItem.roomId!!)]
            notification.bitmapMap[roomItem.lastSenderId!!] = bitmap

            if (notification.bitmapMap[roomItem.roomId!!] != null) {
                pushChatNotificationInternal(roomItem)
            }

            notification.senderAvatarTargets.remove(roomItem.lastSenderId!!)
        }
    }

    override fun getNotificationId(roomId: String): Int {
        return roomId.hashCode()
    }

    private fun getNotificationActionRequestCode(notificationId: Int): Int {
        return notificationId.shl(Constants.NOTIFICATION_REPLY_PENDING_INTENT)
    }
}