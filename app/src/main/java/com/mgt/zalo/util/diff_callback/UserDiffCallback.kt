package com.mgt.zalo.util.diff_callback

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.mgt.zalo.data_model.User
import javax.inject.Inject

class UserDiffCallback @Inject constructor() : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: User, newItem: User): Any? {
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