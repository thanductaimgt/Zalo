package com.mgt.zalo.util.diff_callback

import androidx.recyclerview.widget.DiffUtil
import com.mgt.zalo.data_model.RoomMember

class RoomMemberDiffCallback : DiffUtil.ItemCallback<RoomMember>() {
    override fun areItemsTheSame(oldItem: RoomMember, newItem: RoomMember): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: RoomMember, newItem: RoomMember): Boolean {
        return true
    }
}