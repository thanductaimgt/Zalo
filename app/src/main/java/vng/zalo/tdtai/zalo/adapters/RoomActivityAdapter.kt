package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.item_receive_room_activity.view.*
import kotlinx.android.synthetic.main.item_send_room_activity.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Message
import vng.zalo.tdtai.zalo.utils.*
import vng.zalo.tdtai.zalo.views.activities.RoomActivity

class RoomActivityAdapter(diffCallback: MessageDiffCallback) : ListAdapter<Message, MessageViewHolder>(diffCallback) {
    private val dateFormat = java.text.SimpleDateFormat.getDateInstance()
    private val timeFormat = java.text.SimpleDateFormat.getTimeInstance()

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].senderPhone) {
            ZaloApplication.currentUser!!.phone -> Constants.VIEW_TYPE_SEND
            else -> Constants.VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            Constants.VIEW_TYPE_SEND -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_send_room_activity, parent, false)
                SendViewHolder(v)
            }
            else -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_receive_room_activity, parent, false)
                RecvViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(position)
    }

//    override fun onBindViewHolder(
//            holder: MessageViewHolder,
//            position: Int,
//            payloads: MutableList<Any>
//    ) {
//        if (payloads.isNotEmpty()) {
//            (payloads[0] as ArrayList<*>).forEach {
//                val curMessage = currentList[position]
//                val nextMessage = if (position > 0) currentList[position - 1] else null
//                val prevMessage = if (position < currentList.lastIndex) currentList[position + 1] else null
//                when (it) {
//                    Message.PAYLOAD_TIME -> holder.bindTime(curMessage, prevMessage, nextMessage)
//                    Message.PAYLOAD_DATE -> holder.bindDate(curMessage, prevMessage)
//                }
//            }
//        } else {
//            onBindViewHolder(holder, position)
//        }
//    }

    override fun onViewAttachedToWindow(holder: MessageViewHolder) {
        val recyclerView = (holder.itemView.context as RoomActivity).recyclerView
        if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            holder.itemView.apply {
                when (holder.itemViewType) {
                    Constants.VIEW_TYPE_SEND -> sendMsgAnimView.resumeAnimation()
                    else -> recvMsgAnimView.resumeAnimation()
                }
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: MessageViewHolder) {
        holder.itemView.apply {
            when (holder.itemViewType) {
                Constants.VIEW_TYPE_SEND -> sendMsgAnimView.pauseAnimation()
                else -> recvMsgAnimView.pauseAnimation()
            }
        }
    }

    override fun onViewRecycled(holder: MessageViewHolder) {
        holder.itemView.apply {
            when (holder.itemViewType) {
                Constants.VIEW_TYPE_SEND -> {
                    sendMsgTextView.visibility = View.GONE
                    sendMsgAnimView.visibility = View.GONE
                    sendMsgTimeTextView.visibility = View.GONE
                    sendMsgDateTextView.visibility = View.GONE

                    (sendMsgAnimView.drawable as LottieDrawable?)?.clearComposition()
                }
                else -> {
                    recvMsgTextView.visibility = View.GONE
                    recvMsgAnimView.visibility = View.GONE
                    recvMsgTimeTextView.visibility = View.GONE
                    recvMsgDateTextView.visibility = View.GONE
                    recvAvatarImgView.visibility = View.GONE

                    recvAvatarImgView.setImageDrawable(null)
                    (recvMsgAnimView.drawable as LottieDrawable?)?.clearComposition()
                }
            }
        }
    }

    inner class SendViewHolder(itemView: View) : MessageViewHolder(itemView) {
        override fun bind(position: Int) {
            val curMessage = currentList[position]
            val nextMessage = if (position > 0) currentList[position - 1] else null
            val prevMessage = if (position < currentList.lastIndex) currentList[position + 1] else null

            itemView.apply {
                when (curMessage.type) {
                    Constants.MESSAGE_TYPE_STICKER -> bindSticker(curMessage)
                    else -> bindText(curMessage)
                }
            }

            bindDate(curMessage, prevMessage)
            bindTime(curMessage, prevMessage, nextMessage)
        }

        override fun bindTime(curMessage: Message, prevMessage: Message?, nextMessage: Message?): Boolean {
            itemView.apply {
                /* if one of these is true:
                - current curMessage is last curMessage
                - next curMessage time - current curMessage time < 1 min
                - next curMessage date != current curMessage date
                => display curMessage time
                */
                if (nextMessage == null ||
                        Utils.getTimeDiffInMillis(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                        Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())) {
                    sendMsgTimeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
                    sendMsgTimeTextView.visibility = View.VISIBLE
                    return true
                }
                return false
            }
        }

        override fun bindDate(curMessage: Message, prevMessage: Message?) {
            itemView.apply {
                /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
                if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
                    sendMsgDateTextView.text = dateFormat.format(curMessage.createdTime!!.toDate())
                    sendMsgDateTextView.visibility = View.VISIBLE
                }
            }
        }

        override fun bindSticker(message: Message) {
            itemView.apply {
                sendMsgAnimView.visibility = View.VISIBLE
                LottieCompositionFactory.fromUrl(context, message.content).addListener { lottieComposition ->
                    sendMsgAnimView.apply {
                        post {
                            setComposition(lottieComposition)
                            if (isAttachedToWindow) {
                                resumeAnimation()
                            }
                        }
                    }
                }
            }
        }

        override fun bindText(message: Message) {
            itemView.apply {
                sendMsgTextView.text = message.content
                sendMsgTextView.visibility = View.VISIBLE
            }
        }
    }

    inner class RecvViewHolder(itemView: View) : MessageViewHolder(itemView) {
        override fun bind(position: Int) {
            val curMessage = currentList[position]
            val nextMessage = if (position > 0) currentList[position - 1] else null
            val prevMessage = if (position < currentList.lastIndex) currentList[position + 1] else null

            itemView.apply {
                when (curMessage.type) {
                    Constants.MESSAGE_TYPE_STICKER -> bindSticker(curMessage)
                    else -> bindText(curMessage)
                }

                bindDate(curMessage, prevMessage)

                val isTimeBind = bindTime(curMessage, prevMessage, nextMessage)

                bindAvatar(curMessage, nextMessage, isTimeBind)
            }
        }

        override fun bindTime(curMessage: Message, prevMessage: Message?, nextMessage: Message?): Boolean {
            itemView.apply {
                /* if one of these is true:
                - current message is last message
                - next message time - current message time < 1 min
                - next message date != current message date
                => display message time
                */
                if (nextMessage == null ||
                        Utils.getTimeDiffInMillis(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                        Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())) {
                    recvMsgTimeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
                    recvMsgTimeTextView.visibility = View.VISIBLE
                    return true
                }
                return false
            }
        }

        override fun bindDate(curMessage: Message, prevMessage: Message?) {
            itemView.apply {
                /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
                if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
                    recvMsgDateTextView.text = dateFormat.format(curMessage.createdTime!!.toDate())
                    recvMsgDateTextView.visibility = View.VISIBLE
                }
            }
        }

        override fun bindSticker(message: Message) {
            itemView.apply {
                recvMsgAnimView.visibility = View.VISIBLE
                LottieCompositionFactory.fromUrl(context, message.content).addListener { lottieComposition ->
                    recvMsgAnimView.apply {
                        post {
                            setComposition(lottieComposition)
                            if (isAttachedToWindow) {
                                resumeAnimation()
                            }
                        }
                    }
                }
            }
        }

        override fun bindText(message: Message) {
            itemView.apply {
                recvMsgTextView.apply {
                    text = message.content
                    visibility = View.VISIBLE
                }
            }
        }

        private fun bindAvatar(curMessage: Message, nextMessage: Message?, isTimeBind: Boolean) {
            itemView.apply {
                /* if one of these is true:
                - curMessage time displayed
                - next curMessage sender != current curMessage sender
                => display avatarUrl
                */
                if (isTimeBind || nextMessage!!.senderPhone != curMessage.senderPhone) {
                    recvAvatarImgView.visibility = View.VISIBLE
                    Picasso.get()
                            .load(curMessage.senderAvatarUrl)
                            .fit()
                            .into(recvAvatarImgView)
                }
            }
        }
    }
}