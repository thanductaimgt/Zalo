package vng.zalo.tdtai.zalo.utils

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.models.RoomItem

class RoomItemDiffCallback : DiffUtil.ItemCallback<RoomItem>() {

    override fun areItemsTheSame(oldItem: RoomItem, newItem: RoomItem): Boolean {
        Log.d(TAG, if (oldItem.roomId == newItem.roomId) "same item" else "diff item")
        return oldItem.roomId == newItem.roomId
    }

    override fun areContentsTheSame(oldItem: RoomItem, newItem: RoomItem): Boolean {
        Log.d(TAG, if (oldItem == newItem) "same content" else "diff content")
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: RoomItem, newItem: RoomItem): Any? {
        val res = ArrayList<Int>()
        if (oldItem.lastMsgTime != newItem.lastMsgTime) {
            res.add(RoomItem.PAYLOAD_NEW_MESSAGE)
        }
        if (oldItem.unseenMsgNum != newItem.unseenMsgNum) {
            res.add(RoomItem.PAYLOAD_SEEN_MESSAGE)
        }
        if (oldItem.isOnline != newItem.isOnline) {
            res.add(RoomItem.PAYLOAD_ONLINE_STATUS)
        }
        return res
    }
}