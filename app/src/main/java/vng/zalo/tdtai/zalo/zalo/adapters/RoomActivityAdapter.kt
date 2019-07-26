package vng.zalo.tdtai.zalo.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_receive_room_activity.view.*
import kotlinx.android.synthetic.main.item_send_room_activity.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.Message
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class RoomActivityAdapter(private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val dateFormat = java.text.SimpleDateFormat.getDateInstance()
    private val timeFormat = java.text.SimpleDateFormat.getTimeInstance()
    var messages: List<Message> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].senderPhone) {
            ZaloApplication.currentUser!!.phone -> Constants.VIEW_TYPE_SEND
            else -> Constants.VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ModelViewHolder).bind(position)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            holder.itemView.sendMsgAnimView?.resumeAnimation()
            holder.itemView.recvMsgAnimView?.resumeAnimation()
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        holder.itemView.sendMsgAnimView?.pauseAnimation()
        holder.itemView.recvMsgAnimView?.pauseAnimation()
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
        override fun bind(position: Int) {
            val curMessage = messages[position]
            val prevMessage = if (position > 0) messages[position - 1] else null
            val nextMessage = if (position < itemCount - 1) messages[position + 1] else null

            itemView.apply {
                when (curMessage.type) {
                    Constants.MESSAGE_TYPE_TEXT -> {
                        sendMsgAnimView.visibility = View.GONE
                        sendMsgTextView.visibility = View.VISIBLE
                        sendMsgTextView.apply { text = curMessage.content }
                    }
                    Constants.MESSAGE_TYPE_STICKER -> {
                        sendMsgTextView.visibility = View.GONE
                        sendMsgAnimView.visibility = View.INVISIBLE
                        val animUrl = curMessage.content
                        LottieCompositionFactory.fromUrl(context, animUrl).addListener { lottieComposition ->
                            sendMsgAnimView.apply {
                                setComposition(lottieComposition)
                                sendMsgAnimView.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
                if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
                    sendMsgDateTextView.text = dateFormat.format(curMessage.createdTime!!.toDate())
                    sendMsgDateTextView.visibility = View.VISIBLE
                } else {
                    sendMsgDateTextView.visibility = View.GONE
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
                    sendMsgTimeTextView.visibility = View.VISIBLE
                    sendMsgTimeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
                } else {
                    sendMsgTimeTextView.visibility = View.GONE
                }
            }
        }
    }

    inner class RecvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
        override fun bind(position: Int) {
            val curMessage = messages[position]
            val prevMessage = if (position > 0) messages[position - 1] else null
            val nextMessage = if (position < itemCount - 1) messages[position + 1] else null

            itemView.apply {
                when (curMessage.type) {
                    Constants.MESSAGE_TYPE_TEXT -> {
                        recvMsgAnimView.visibility = View.GONE
                        recvMsgTextView.visibility = View.VISIBLE
                        recvMsgTextView.apply { text = curMessage.content }
                    }
                    Constants.MESSAGE_TYPE_STICKER -> {
                        recvMsgTextView.visibility = View.GONE
                        recvMsgAnimView.visibility = View.INVISIBLE
                        LottieCompositionFactory.fromUrl(context, curMessage.content).addListener { lottieComposition ->
                            recvMsgAnimView.apply {
                                setComposition(lottieComposition)
                                recvMsgAnimView.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
                if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
                    recvMsgDateTextView.text = dateFormat.format(curMessage.createdTime!!.toDate())
                    recvMsgDateTextView.visibility = View.VISIBLE
                } else {
                    recvMsgDateTextView.visibility = View.GONE
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
                    recvMsgTimeTextView.visibility = View.VISIBLE
                    recvMsgTimeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
                    displayMessageTime = true
                } else {
                    recvMsgTimeTextView.visibility = View.GONE
                }

                /* if one of these is true:
                - message time displayed
                - next message sender != current message sender
                => display avatarUrl
                */
                if (displayMessageTime || nextMessage!!.senderPhone != curMessage.senderPhone) {
                    Picasso.get()
                            .load(curMessage.senderAvatarUrl)
                            .fit()
                            .into(recvAvatarImgView)
                    recvAvatarImgView.visibility = View.VISIBLE
                } else {
                    recvAvatarImgView.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private val TAG = RoomActivityAdapter::class.java.simpleName
    }
}