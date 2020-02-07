package vng.zalo.tdtai.zalo.models.message

import android.content.Context
import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.models.RoomMember

data class SeenMessage(
        override var id: String? = null,
        override var createdTime: Timestamp? = null,
        override var senderPhone: String? = null,
        override var senderAvatarUrl: String? = null,
        override var isSent: Boolean=false,
        var seenMembers:List<RoomMember> =ArrayList()
) :Message(id, createdTime, senderPhone, senderAvatarUrl, TYPE_SEEN, isSent){
    override fun getPreviewContent(context: Context): String {
        return "Seen =)"
    }

    companion object{
        const val ID = "SeenMessage"
    }
}