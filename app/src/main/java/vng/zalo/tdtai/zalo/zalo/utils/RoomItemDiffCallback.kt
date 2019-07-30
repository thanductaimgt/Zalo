package vng.zalo.tdtai.zalo.zalo.utils

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.zalo.models.RoomItem

class RoomItemDiffCallback : DiffUtil.ItemCallback<RoomItem>() {

    override fun areItemsTheSame(oldItem: RoomItem, newItem: RoomItem): Boolean {
        Log.d(TAG, if (oldItem.roomId == newItem.roomId) "same item" else "diff item")
        return oldItem.roomId == newItem.roomId
    }

    override fun areContentsTheSame(oldItem: RoomItem, newItem: RoomItem): Boolean {
        Log.d(TAG, if (oldItem == newItem) "same content" else "diff content")
        return oldItem == newItem
    }

    companion object{
        private val TAG = RoomItemDiffCallback::class.java.simpleName
    }
}