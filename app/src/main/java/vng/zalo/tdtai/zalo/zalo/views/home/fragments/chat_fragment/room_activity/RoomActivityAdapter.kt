package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_receive_room_activity.view.*
import kotlinx.android.synthetic.main.item_send_room_activity.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.Message
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder
import vng.zalo.tdtai.zalo.zalo.utils.Utils

//class RoomActivityAdapter(diffCallback: DiffUtil.ItemCallback<Message>) : ListAdapter<Message, RecyclerView.ViewHolder>(diffCallback) {
//    private val dateFormat = java.text.SimpleDateFormat.getDateInstance()
//    private val timeFormat = java.text.SimpleDateFormat.getTimeInstance()
//
//    override fun getItemViewType(position: Int): Int {
//        return when (getItem(position).senderPhone) {
//            ZaloApplication.currentUser!!.phone -> Constants.VIEW_TYPE_SENDER
//            else -> Constants.VIEW_TYPE_RECEIVER
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return when (viewType) {
//            Constants.VIEW_TYPE_SENDER -> {
//                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_send_room_activity, parent, false)
//                SenderViewHolder(v)
//            }
//            else -> {
//                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_receive_room_activity, parent, false)
//                ReceiverViewHolder(v)
//            }
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        (holder as ModelViewHolder).bind(position)
//    }
//
//    override fun getItemCount(): Int {
//        return currentList.size
//    }
//
//    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
//        override fun bind(position: Int) {
//            val curMessage = getItem(position)
//            val prevMessage = if (position > 0) getItem(position - 1) else null
//            val nextMessage = if (position < itemCount - 1) getItem(position + 1) else null
//
//            with(itemView) {
//                sendMsgTextView.text = curMessage.content
//
//                /* if one of these is true:
//                - current message is first message
//                - previous message date != current message date
//                => display date
//                */
//                if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
//                    dateTextViewItemSend.text = dateFormat.format(curMessage.createdTime!!.toDate())
//                    dateTextViewItemSend.visibility = View.VISIBLE
//                } else {
//                    dateTextViewItemSend.visibility = View.GONE
//                }
//
//                /* if one of these is true:
//                - current message is last message
//                - next message time - current message time < 1 min
//                - next message date != current message date
//                => display message time
//                */
//                if (nextMessage == null ||
//                        Utils.timeGapInMillisecond(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
//                        Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())) {
//                    sendTimeTextView.visibility = View.VISIBLE
//                    sendTimeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
//                } else {
//                    sendTimeTextView.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
//        override fun bind(position: Int) {
//            val curMessage = getItem(position)
//            val prevMessage = if (position > 0) getItem(position - 1) else null
//            val nextMessage = if (position < itemCount - 1) getItem(position + 1) else null
//
//            with(itemView) {
//                receiveMsgTextView.text = curMessage.content
//
//                /* if one of these is true:
//                - current message is first message
//                - previous message date != current message date
//                => display date
//                */
//                if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
//                    dateTextViewItemRecv.text = dateFormat.format(curMessage.createdTime!!.toDate())
//                    dateTextViewItemRecv.visibility = View.VISIBLE
//                } else {
//                    dateTextViewItemRecv.visibility = View.GONE
//                }
//
//                /* if one of these is true:
//                - current message is last message
//                - next message time - current message time < 1 min
//                - next message date != current message date
//                => display message time
//                */
//                var displayMessageTime = false
//                if (nextMessage == null ||
//                        Utils.timeGapInMillisecond(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
//                        Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())) {
//                    recvTimeTextView.visibility = View.VISIBLE
//                    recvTimeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
//                    displayMessageTime = true
//                } else {
//                    recvTimeTextView.visibility = View.GONE
//                }
//
//                /* if one of these is true:
//                - message time displayed
//                - next message sender != current message sender
//                => display avatar
//                */
//                if (displayMessageTime || nextMessage!!.senderPhone != curMessage.senderPhone) {
//                    Picasso.get()
//                            .load(curMessage.senderAvatar)
//                            .fit()
//                            .into(receiveAvatarImgView)
//                    receiveAvatarImgView.visibility = View.VISIBLE
//                } else {
//                    receiveAvatarImgView.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//    companion object {
//        private val TAG = RoomActivityAdapter::class.java.simpleName
//    }
//}
class RoomActivityAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val dateFormat = java.text.SimpleDateFormat.getDateInstance()
    private val timeFormat = java.text.SimpleDateFormat.getTimeInstance()
    var messages:List<Message> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].senderPhone) {
            ZaloApplication.currentUser!!.phone -> Constants.VIEW_TYPE_SENDER
            else -> Constants.VIEW_TYPE_RECEIVER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Constants.VIEW_TYPE_SENDER -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_send_room_activity, parent, false)
                SenderViewHolder(v)
            }
            else -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_receive_room_activity, parent, false)
                ReceiverViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ModelViewHolder).bind(position)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
        override fun bind(position: Int) {
            val curMessage = messages[position]
            val prevMessage = if (position > 0) messages[position-1] else null
            val nextMessage = if (position < itemCount - 1) messages[position+1] else null

            itemView.apply {
                sendMsgTextView.text = curMessage.content

                /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
                if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
                    dateTextViewItemSend.text = dateFormat.format(curMessage.createdTime!!.toDate())
                    dateTextViewItemSend.visibility = View.VISIBLE
                } else {
                    dateTextViewItemSend.visibility = View.GONE
                }

                /* if one of these is true:
                - current message is last message
                - next message time - current message time < 1 min
                - next message date != current message date
                => display message time
                */
                if (nextMessage == null ||
                        Utils.timeGapInMillisecond(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                        Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())) {
                    sendTimeTextView.visibility = View.VISIBLE
                    sendTimeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
                } else {
                    sendTimeTextView.visibility = View.GONE
                }
            }
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
        override fun bind(position: Int) {
            val curMessage = messages[position]
            val prevMessage = if (position > 0) messages[position-1] else null
            val nextMessage = if (position < itemCount - 1) messages[position+1] else null

            with(itemView) {
                receiveMsgTextView.text = curMessage.content

                /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
                if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
                    dateTextViewItemRecv.text = dateFormat.format(curMessage.createdTime!!.toDate())
                    dateTextViewItemRecv.visibility = View.VISIBLE
                } else {
                    dateTextViewItemRecv.visibility = View.GONE
                }

                /* if one of these is true:
                - current message is last message
                - next message time - current message time < 1 min
                - next message date != current message date
                => display message time
                */
                var displayMessageTime = false
                if (nextMessage == null ||
                        Utils.timeGapInMillisecond(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                        Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())) {
                    recvTimeTextView.visibility = View.VISIBLE
                    recvTimeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
                    displayMessageTime = true
                } else {
                    recvTimeTextView.visibility = View.GONE
                }

                /* if one of these is true:
                - message time displayed
                - next message sender != current message sender
                => display avatar
                */
                if (displayMessageTime || nextMessage!!.senderPhone != curMessage.senderPhone) {
                    Picasso.get()
                            .load(curMessage.senderAvatar)
                            .fit()
                            .into(receiveAvatarImgView)
                    receiveAvatarImgView.visibility = View.VISIBLE
                } else {
                    receiveAvatarImgView.visibility = View.GONE
                }
            }
        }
    }
}