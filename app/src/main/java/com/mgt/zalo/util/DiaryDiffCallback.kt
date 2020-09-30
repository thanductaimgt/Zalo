package com.mgt.zalo.util

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.mgt.zalo.data_model.post.Diary
import javax.inject.Inject

class DiaryDiffCallback @Inject constructor() : DiffUtil.ItemCallback<Diary>() {
    override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Diary, newItem: Diary): Any? {
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