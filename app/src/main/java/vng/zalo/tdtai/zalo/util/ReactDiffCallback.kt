package vng.zalo.tdtai.zalo.util

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.data_model.react.React
import javax.inject.Inject

class ReactDiffCallback @Inject constructor() : DiffUtil.ItemCallback<React>() {
    override fun areItemsTheSame(oldItem: React, newItem: React): Boolean {
        return oldItem.ownerId == newItem.ownerId
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: React, newItem: React): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: React, newItem: React): Any? {
        val res = ArrayList<Int?>()
//        if(oldItem is SeenMessage){
//            if (oldItem.seenMembers != (newItem as SeenMessage).seenMembers) {
//                res.add(Message.PAYLOAD_SEEN)
//            }
//        }else{
//            if (oldItem.senderAvatarUrl != newItem.senderAvatarUrl) {
//                res.add(Message.PAYLOAD_AVATAR)
//            }
//            if (oldItem is ResourceMessage && oldItem.uploadProgress != (newItem as ResourceMessage).uploadProgress) {
//                res.add(Message.PAYLOAD_UPLOAD_PROGRESS)
//            }
//            if (oldItem.isSent != newItem.isSent) {
//                res.add(Message.PAYLOAD_SEND_STATUS)
//            }
//        }
        return res
    }
}