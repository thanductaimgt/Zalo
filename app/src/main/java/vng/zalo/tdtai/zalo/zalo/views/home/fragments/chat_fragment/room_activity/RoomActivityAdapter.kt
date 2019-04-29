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

class RoomActivityAdapter(diffCallback: DiffUtil.ItemCallback<Message>) : ListAdapter<Message, RecyclerView.ViewHolder>(diffCallback) {
    private val dateFormat = java.text.SimpleDateFormat.getDateInstance()
    private val timeFormat = java.text.SimpleDateFormat.getTimeInstance()

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).senderPhone){
            ZaloApplication.currentUser.phone -> Constants.VIEW_TYPE_SENDER
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
        return currentList.size
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
        override fun bind(position: Int) {
            val curMessage = getItem(position)
            val prevMessage = if (position > 0) getItem(position - 1) else null
            val nextMessage = if (position < itemCount - 1) getItem(position + 1) else null

            itemView.sendMsgTextView.text = curMessage.content

            /* if one of these is true:
            - current message is first message
            - previous message date != current message date
            => display date
            */
            if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime.toDate(), prevMessage.createdTime.toDate())) {
                itemView.dateTextViewItemSend.text = dateFormat.format(curMessage.createdTime.toDate())
                itemView.dateTextViewItemSend.visibility = View.VISIBLE
            } else {
                itemView.dateTextViewItemSend.visibility = View.GONE
            }

            /* if one of these is true:
            - current message is last message
            - next message time - current message time < 1 min
            - next message date != current message date
            => display message time
            */
            if (nextMessage == null ||
                    Utils.timeGapInMillisecond(curMessage.createdTime.toDate(), nextMessage.createdTime.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                    Utils.areInDifferentDay(curMessage.createdTime.toDate(), nextMessage.createdTime.toDate())) {
                itemView.sendTimeTextView.visibility = View.VISIBLE
                itemView.sendTimeTextView.text = timeFormat.format(curMessage.createdTime.toDate())
            } else {
                itemView.sendTimeTextView.visibility = View.GONE
            }
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
        override fun bind(position: Int) {
            val curMessage = getItem(position)
            val prevMessage = if (position > 0) getItem(position - 1) else null
            val nextMessage = if (position < itemCount - 1) getItem(position + 1) else null

            itemView.receiveMsgTextView.text = curMessage.content

            /* if one of these is true:
            - current message is first message
            - previous message date != current message date
            => display date
            */
            if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime.toDate(), prevMessage.createdTime.toDate())) {
                itemView.dateTextViewItemRecv.text = dateFormat.format(curMessage.createdTime.toDate())
                itemView.dateTextViewItemRecv.visibility = View.VISIBLE
            } else {
                itemView.dateTextViewItemRecv.visibility = View.GONE
            }

            /* if one of these is true:
            - current message is last message
            - next message time - current message time < 1 min
            - next message date != current message date
            => display message time
            */
            var displayMessageTime = false
            if (nextMessage == null ||
                    Utils.timeGapInMillisecond(curMessage.createdTime.toDate(), nextMessage.createdTime.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                    Utils.areInDifferentDay(curMessage.createdTime.toDate(), nextMessage.createdTime.toDate())) {
                itemView.recvTimeTextView.visibility = View.VISIBLE
                itemView.recvTimeTextView.text = timeFormat.format(curMessage.createdTime.toDate())
                displayMessageTime = true
            } else {
                itemView.recvTimeTextView.visibility = View.GONE
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
                        .into(itemView.receiveAvatarImgView)
                itemView.receiveAvatarImgView.visibility = View.VISIBLE
            } else {
                itemView.receiveAvatarImgView.visibility = View.GONE
            }
        }
    }

    companion object {
        private val TAG = RoomActivityAdapter::class.java.simpleName
    }
}