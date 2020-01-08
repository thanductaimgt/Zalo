package vng.zalo.tdtai.zalo.abstracts

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.message.*
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.networks.Storage
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils
import java.util.*

class MessageCreator {
    fun addNewMessagesToFirestore(context: Context, room: Room, contents: List<String>, messageType: Int) {
        var curTimeStamp = System.currentTimeMillis()

        contents.forEach { content ->
            if (messageType == Message.TYPE_FILE || messageType == Message.TYPE_IMAGE) {
                val contentUri = Uri.parse(content)
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(contentUri))

                val fileStoragePath = "${Constants.FOLDER_ROOM_DATA}/${room.id}/file_${curTimeStamp}.$extension"
                Storage.addFileAndGetDownloadUrl(context, content, fileStoragePath) { downloadUrl ->
                    val message:Message

                    if (messageType == Message.TYPE_FILE) {
                        message = FileMessage(
                                createdTime = Timestamp(Date(curTimeStamp)),
                                senderPhone = ZaloApplication.curUser!!.phone,
                                senderAvatarUrl = ZaloApplication.curUser!!.avatarUrl,
                                type = messageType,
                                url = downloadUrl
                        )

                        // resolve file info
                        with(context.contentResolver.query(contentUri, null, null, null, null)) {
                            this?.let {
                                moveToFirst()

                                message.fileName = getString(getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                message.fileSize = getLong(getColumnIndex(OpenableColumns.SIZE))
                            }
                        }
                    } else {
                        val ratio = Utils.getImageDimension(context, content)
                        message = ImageMessage(
                                createdTime = Timestamp(Date(curTimeStamp)),
                                senderPhone = ZaloApplication.curUser!!.phone,
                                senderAvatarUrl = ZaloApplication.curUser!!.avatarUrl,
                                type = messageType,
                                url = downloadUrl,
                                ratio = ratio
                        )
                    }

                    Database.addNewMessageAndUpdateUsersRoom(
                            context,
                            curRoom = room,
                            newMessage = message
                    )
                }
            } else if (messageType == Message.TYPE_CALL) {
                val data = content.split('.')
                val callType = data[0].toInt()
                val callTime = data[1].toInt()
                val isMissed = data[2].toBoolean()
                val isCanceled = data[3].toBoolean()

                val message = CallMessage(
                        createdTime = Timestamp(Date(curTimeStamp)),
                        senderPhone = ZaloApplication.curUser!!.phone,
                        senderAvatarUrl = ZaloApplication.curUser!!.avatarUrl,
                        type = messageType,
                        callType = callType,
                        callTime = callTime,
                        isMissed = isMissed,
                        isCanceled = isCanceled
                )

                Database.addNewMessageAndUpdateUsersRoom(
                        context,
                        curRoom = room,
                        newMessage = message
                )
            } else {
                val message = TextMessage(
                        createdTime = Timestamp(Date(curTimeStamp)),
                        senderPhone = ZaloApplication.curUser!!.phone,
                        senderAvatarUrl = ZaloApplication.curUser!!.avatarUrl,
                        type = messageType,
                        content = content
                )

                Database.addNewMessageAndUpdateUsersRoom(
                        context,
                        curRoom = room,
                        newMessage = message
                )
            }

            curTimeStamp++
        }
    }
}