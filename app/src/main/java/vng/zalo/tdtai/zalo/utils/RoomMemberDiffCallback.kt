package vng.zalo.tdtai.zalo.utils

import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.model.RoomMember
import javax.inject.Inject

class RoomMemberDiffCallback : DiffUtil.ItemCallback<RoomMember>() {
    override fun areItemsTheSame(oldItem: RoomMember, newItem: RoomMember): Boolean {
        return oldItem.phone == newItem.phone
    }

    override fun areContentsTheSame(oldItem: RoomMember, newItem: RoomMember): Boolean {
        return true
    }
}