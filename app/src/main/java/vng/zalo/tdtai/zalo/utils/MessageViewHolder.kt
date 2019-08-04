package vng.zalo.tdtai.zalo.utils

import android.view.View
import vng.zalo.tdtai.zalo.models.Message

abstract class MessageViewHolder(itemView: View):BindableViewHolder(itemView) {
    abstract fun bindTime(curMessage: Message, prevMessage: Message?, nextMessage: Message?):Boolean
    abstract fun bindDate(curMessage: Message, prevMessage: Message?)
    abstract fun bindSticker(message: Message)
    abstract fun bindText(message: Message)
}