package vng.zalo.tdtai.zalo.data_model

import android.graphics.Bitmap
import androidx.core.app.Person
import vng.zalo.tdtai.zalo.service.AlwaysRunningNotificationService
import java.util.*
import kotlin.collections.HashMap

data class ChatNotification(
        val id:Int,
        var lastMessages:Queue<ChatNotificationMessage> = LinkedList(),
        var roomAvatarTarget: AlwaysRunningNotificationService.RoomAvatarTarget?=null,
        var senderAvatarTargets: HashMap<String, AlwaysRunningNotificationService.SenderAvatarTarget> = HashMap()
){
    val bitmapMap = HashMap<String, Bitmap>()

    fun addMessage(message:ChatNotificationMessage){
        if(lastMessages.size == MAX_MESSAGE_NUM){
            lastMessages.poll()
        }
        lastMessages.offer(message)
    }

    fun containsMessage(createdTime:Long):Boolean{
        return lastMessages.any { it.createdTime == createdTime }
    }



    class ChatNotificationMessage(
            val createdTime:Long,
            val content:String,
            val senderPerson: Person
    )

    companion object{
        const val MAX_MESSAGE_NUM = 5
    }
}