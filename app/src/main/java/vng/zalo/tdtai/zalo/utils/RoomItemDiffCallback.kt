package vng.zalo.tdtai.zalo.utils

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.room.RoomItemGroup
import vng.zalo.tdtai.zalo.models.room.RoomItemPeer

class RoomItemDiffCallback : DiffUtil.ItemCallback<RoomItem>() {

    override fun areItemsTheSame(oldItem: RoomItem, newItem: RoomItem): Boolean {
        Log.d(TAG, if (oldItem.roomId == newItem.roomId) "same item" else "diff item")
        return oldItem.roomId == newItem.roomId
    }

    override fun areContentsTheSame(oldItem: RoomItem, newItem: RoomItem): Boolean {
        val res = when (oldItem.roomType) {
            Room.TYPE_PEER -> (oldItem as RoomItemPeer) == newItem
            else -> (oldItem as RoomItemGroup) == newItem
        }
        Log.d(TAG, if(res) "same content" else "diff content")
        return res
    }

    override fun getChangePayload(oldItem: RoomItem, newItem: RoomItem): Any? {
        val res = ArrayList<Int>()
        if (oldItem.lastMsgTime != newItem.lastMsgTime) {
            res.add(RoomItem.PAYLOAD_NEW_MESSAGE)
        }
        if (oldItem.unseenMsgNum != newItem.unseenMsgNum) {
            res.add(RoomItem.PAYLOAD_SEEN_MESSAGE)
        }
        if (oldItem.roomType == Room.TYPE_PEER) {
            oldItem as RoomItemPeer
            newItem as RoomItemPeer
            if (oldItem.lastOnlineTime != newItem.lastOnlineTime && (oldItem.lastOnlineTime == null || newItem.lastOnlineTime == null)) {
                res.add(RoomItem.PAYLOAD_ONLINE_STATUS)
            }
        }
        if (oldItem.lastTypingPhone != newItem.lastTypingPhone) {
            res.add(RoomItem.PAYLOAD_TYPING)
        }
        return res
    }
}