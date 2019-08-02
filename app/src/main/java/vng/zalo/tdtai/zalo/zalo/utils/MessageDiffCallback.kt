package vng.zalo.tdtai.zalo.zalo.utils

import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.zalo.models.Message

class MessageDiffCallback: DiffUtil.ItemCallback<Message>() {

    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.createdTime == newItem.createdTime
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}