package vng.zalo.tdtai.zalo.zalo.utils

import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.zalo.models.RoomItem

//    @Inject
class RoomItemDiffCallback : DiffUtil.ItemCallback<RoomItem>() {

    override fun areItemsTheSame(oldItem: RoomItem, newItem: RoomItem): Boolean {
        return oldItem.roomId == newItem.roomId
    }

    override fun areContentsTheSame(oldItem: RoomItem, newItem: RoomItem): Boolean {
        return oldItem == newItem
    }

    companion object {
        private val TAG = RoomItemDiffCallback::class.java.simpleName
    }
}