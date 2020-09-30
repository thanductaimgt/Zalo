package com.mgt.zalo.data_model.message

import android.content.Context
import com.mgt.zalo.data_model.RoomMember

data class SeenMessage(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var senderId: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false,
        var seenMembers:List<RoomMember> =ArrayList()
) :Message(id, createdTime, senderId, senderAvatarUrl, TYPE_SEEN, isSent){
    override fun getPreviewContent(context: Context): String {
        return "Seen =)"
    }

    companion object{
        const val ID = "SeenMessage"
    }
}