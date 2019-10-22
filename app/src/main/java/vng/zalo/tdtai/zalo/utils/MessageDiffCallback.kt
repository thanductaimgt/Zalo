package vng.zalo.tdtai.zalo.utils

import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.models.Message

class MessageDiffCallback: DiffUtil.ItemCallback<Message>() {

    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.createdTime == newItem.createdTime
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Message, newItem: Message): Any? {
        val res = ArrayList<Int>()
        if (oldItem.senderAvatarUrl != newItem.senderAvatarUrl) {
            res.add(Message.PAYLOAD_AVATAR)
        }
        return res
    }
}