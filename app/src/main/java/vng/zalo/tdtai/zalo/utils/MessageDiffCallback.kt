package vng.zalo.tdtai.zalo.utils

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.models.message.ResourceMessage
import vng.zalo.tdtai.zalo.models.message.SeenMessage

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {

    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Message, newItem: Message): Any? {
        val res = ArrayList<Int?>()
        if(oldItem is SeenMessage){
            if (oldItem.seenMembers != (newItem as SeenMessage).seenMembers) {
                res.add(Message.PAYLOAD_SEEN)
            }
        }else{
            if (oldItem.senderAvatarUrl != newItem.senderAvatarUrl) {
                res.add(Message.PAYLOAD_AVATAR)
            }
            if (oldItem is ResourceMessage && oldItem.uploadProgress != (newItem as ResourceMessage).uploadProgress) {
                res.add(Message.PAYLOAD_UPLOAD_PROGRESS)
            }
            if (oldItem.isSent != newItem.isSent) {
                res.add(Message.PAYLOAD_SEND_STATUS)
            }
        }
        return res
    }
}