package vng.zalo.tdtai.zalo.abstracts

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.message.CallMessage
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.networks.Storage
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils
import java.util.*

class MessageCreator {
    fun addNewMessagesToFirestore(context: Context, room: Room, contents: List<String>, type: Int) {
        var curTimeStamp = System.currentTimeMillis()

        contents.forEach { content ->
            val message = Message(
                    content = content,
                    createdTime = Timestamp(Date(curTimeStamp)),
                    senderPhone = ZaloApplication.curUser!!.phone,
                    senderAvatarUrl = ZaloApplication.curUser!!.avatarUrl,
                    type = type
            )

            if (message.type == Message.TYPE_FILE || message.type == Message.TYPE_IMAGE) {
                val contentUri = Uri.parse(content)
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(contentUri))

                val fileStoragePath = "${Constants.FOLDER_ROOM_DATA}/${room.id}/file_${curTimeStamp}.$extension"
                Storage.addFileAndGetDownloadUrl(context, content, fileStoragePath) { downloadUrl ->
                    if (message.type == Message.TYPE_FILE) {
                        // resolve file info
                        with(context.contentResolver.query(contentUri, null, null, null, null)) {
                            this?.let {
                                moveToFirst()
                                message.fileName = getString(getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                message.fileSize = getLong(getColumnIndex(OpenableColumns.SIZE))
                            }
                        }
                    } else {
                        message.ratio = Utils.getImageDimension(context, message.content!!)
                    }
                    message.content = downloadUrl

                    Database.addNewMessageAndUpdateUsersRoom(
                            context,
                            curRoom = room,
                            newMessage = message
                    )
                }
            } else if (message.type == Message.TYPE_CALL) {
                val data = content.split('.')
                val callType = data[0].toInt()
                val callTime = data[1].toInt()
                val isMissed = data[2].toBoolean()
                val isCanceled = data[3].toBoolean()

                val message = CallMessage(
                        content = content,
                        createdTime = Timestamp(Date(curTimeStamp)),
                        senderPhone = ZaloApplication.curUser!!.phone,
                        senderAvatarUrl = ZaloApplication.curUser!!.avatarUrl,
                        type = type,
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