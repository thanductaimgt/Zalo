package com.mgt.zalo.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseService
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.base.EmptyActivity
import com.mgt.zalo.data_model.media.ImageMedia
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.data_model.post.Diary
import com.mgt.zalo.data_model.post.Post
import com.mgt.zalo.repository.Storage
import com.mgt.zalo.ui.SplashActivity
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG

class UploadService : BaseService() {
    private val notificationBuilders = hashMapOf<Int, NotificationCompat.Builder>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (intent.action) {
                ACTION_CANCEL -> {
                    val notificationId = intent.getIntExtra(NOTIFICATION_ID, -1)
                    if (notificationId >= 0) {
                        NotificationManagerCompat.from(this).cancel(notificationId)
                    }
                }
                ACTION_CREATE -> {
                    val diary = intent.getParcelableExtra<Diary>(KEY_DIARY)!!
                    val notificationId = getNotificationId(diary)
                    val notificationBuilder = createNotificationBuilder(notificationId)

                    startForeground(notificationId, notificationBuilder.build())
                    notificationBuilders[notificationId] = notificationBuilder

                    createDiary(diary, notificationId)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationBuilder(notificationId: Int): NotificationCompat.Builder {
        // create the pending intent and add to the notification
        val notificationIntent = Intent(application, SplashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val notificationPendingIntent = PendingIntent.getActivity(
                application,
                notificationId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelIntent = Intent(application, UploadService::class.java).apply {
            action = ACTION_CANCEL
            putExtra(NOTIFICATION_ID, notificationId)
        }

        return NotificationCompat.Builder(application, Constants.UPLOAD_NOTIFY_CHANNEL_ID)
                .setContentTitle(getString(R.string.label_upload_service))
                .setContentText(getString(R.string.description_uploading_post))
                .setColor(ContextCompat.getColor(application, R.color.lightPrimary))
                .setContentIntent(notificationPendingIntent)
                .setSmallIcon(R.drawable.app_icon_transparent)
//                .setLargeIcon(roomAvatarBitmap)
                .addAction(R.drawable.cancel, application.getString(R.string.label_cancel),
                        PendingIntent.getActivity(application, getNotificationActionRequestCode(notificationId), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
    }

    private fun getNotificationId(diary: Diary): Int {
        return diary.id!!.hashCode()
    }

    private fun getNotificationActionRequestCode(notificationId: Int): Int {
        return notificationId + 1
    }

    private fun createDiary(diary: Diary, notificationId: Int) {
        diary.type = if (diary.medias.any { it is VideoMedia }) Post.TYPE_WATCH else Post.TYPE_DIARY

        val storagePath = storage.getPostStoragePath(diary)

        var uploadCount = 0
        var uploadedCount = 0
        val totalCount = diary.medias.size
        val uploadProgresses = IntArray(totalCount)

        val checkAllUploadCompletedAndCreateDiary = {
            if (uploadCount == totalCount) {
                if (uploadedCount == uploadCount) {
                    database.createPost(diary) { isSuccess ->
                        if (isSuccess) {
                            val successNotification = notificationBuilders[notificationId]!!
                                    .setContentText(getString(R.string.description_upload_success))
                                    .setProgress(0, 0, false)
                                    .setAutoCancel(true)
                                    .apply { mActions.clear() }
                                    .build()

                            // create the pending intent and add to the notification
                            val notificationIntent = Intent(application, EmptyActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra(EmptyActivity.KEY_FRAGMENT, BaseView.FRAGMENT_POST_DETAIL)
                                putExtra(EmptyActivity.KEY_DIARY, diary)
                            }
                            val successPendingIntent = PendingIntent.getActivity(
                                    application,
                                    notificationId,
                                    notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            NotificationManagerCompat.from(this).notify(
                                    notificationId,
                                    successNotification.apply {
                                        contentIntent = successPendingIntent
                                    }
                            )
                        } else {
                            NotificationManagerCompat.from(this).notify(
                                    notificationId,
                                    notificationBuilders[notificationId]!!
                                            .setContentText(getString(R.string.description_upload_fail))
                                            .setProgress(0, 0, false)
                                            .setAutoCancel(true)
                                            .apply { mActions.clear() }
                                            .build()
                            )
                        }
                        stopForeground(Service.STOP_FOREGROUND_DETACH);
                    }
                } else {
                    Log.d(TAG, "uploadCount: $uploadCount, uploadedCount: $uploadedCount, totalCount: $totalCount")
                }
            } else {
                NotificationManagerCompat.from(this).notify(
                        notificationId,
                        notificationBuilders[notificationId]!!
                                .setContentText(getString(R.string.description_upload_fail))
                                .setProgress(0, 0, false)
                                .setAutoCancel(true)
                                .apply { mActions.clear() }
                                .build()
                )
            }
        }

        var lastProgress = -1
        val onProgressChange = {
            val curProgress = uploadProgresses.sum() / totalCount
            if (lastProgress != curProgress) {
                NotificationManagerCompat.from(this).notify(
                        notificationId,
                        notificationBuilders[notificationId]!!
                                .setProgress(100, curProgress, false)
                                .build()
                )
                lastProgress = curProgress
            }
        }

        if (diary.medias.isNotEmpty()) {
            diary.medias.forEachIndexed { index, media ->
                storage.addFileAndGetDownloadUrl(
                        localPath = media.uri!!,
                        storagePath = "$storagePath/$index",
                        fileType = if (media is ImageMedia) Storage.FILE_TYPE_IMAGE else Storage.FILE_TYPE_VIDEO,
                        onProgressChange = { progress ->
                            uploadProgresses[index] = progress
                            onProgressChange()
                        },
                        onComplete = { url ->
                            diary.medias[index].uri = url
                            uploadedCount++
                            checkAllUploadCompletedAndCreateDiary()
                        }
                )
                uploadCount++
            }
        } else {
            checkAllUploadCompletedAndCreateDiary()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_CANCEL = "ACTION_CANCEL"
        const val ACTION_CREATE = "ACTION_CREATE"

        const val NOTIFICATION_ID = "NOTIFICATION_ID"
        const val KEY_DIARY = "DIARY"
    }
}
