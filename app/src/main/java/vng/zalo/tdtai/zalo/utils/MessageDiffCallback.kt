package vng.zalo.tdtai.zalo.utils

import androidx.recyclerview.widget.DiffUtil
import vng.zalo.tdtai.zalo.models.Message

class MessageDiffCallback: DiffUtil.ItemCallback<Message>() {

    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.createdTime == newItem.createdTime
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

//    override fun getChangePayload(oldItem: Message, newItem: Message): Any? {
//        val res = ArrayList<Int>()
//        if (oldItem.state != newItem.state) {
//            res.add(DownloadTask.PAYLOAD_STATE)
//        }
//        if (oldItem.downloadedBytes != newItem.downloadedBytes) {
//            res.add(DownloadTask.PAYLOAD_PROCESS)
//        }
////        if (oldItem.totalBytes != newItem.totalBytes) {
////            res.add(DownloadTask.PAYLOAD_SIZE)
////        }
//        return res
//    }
}