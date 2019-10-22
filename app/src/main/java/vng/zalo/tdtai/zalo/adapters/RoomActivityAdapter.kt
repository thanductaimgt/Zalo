package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.squareup.picasso.Picasso
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Message
import vng.zalo.tdtai.zalo.utils.BindableViewHolder
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.MessageDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.views.activities.RoomActivity

class RoomActivityAdapter(private val roomActivity: RoomActivity, diffCallback: MessageDiffCallback) : ListAdapter<Message, RoomActivityAdapter.MessageViewHolder>(diffCallback) {
    private val dateFormat = java.text.SimpleDateFormat.getDateInstance()
    private val timeFormat = java.text.SimpleDateFormat.getTimeInstance()

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].senderPhone) {
            ZaloApplication.currentUser!!.phone -> VIEW_TYPE_SEND
            else -> VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            VIEW_TYPE_SEND -> {
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

    override fun onBindViewHolder(
            holder: MessageViewHolder,
            position: Int,
            payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val curMessage = currentList[position]
            val nextMessage = getNextNotTypingMessage(position)
//            val prevMessage = getPreviousNotTypingMessage(position)
            (payloads[0] as ArrayList<*>).forEach {
                when (it) {
                    Message.PAYLOAD_TIME -> holder.bindTime(curMessage, nextMessage)
                    Message.PAYLOAD_AVATAR -> {
                        when (holder.itemViewType) {
                            VIEW_TYPE_RECEIVE -> (holder as RecvViewHolder).apply {
                                bindAvatar(curMessage, nextMessage, isBindTime(curMessage, nextMessage))
                            }
                        }
                    }
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onViewAttachedToWindow(holder: MessageViewHolder) {
        if (roomActivity.isRecyclerViewIdle()) {
            holder.stickerAnimView.resumeAnimation()
        }
    }

    override fun onViewRecycled(holder: MessageViewHolder) {
        holder.apply {
            contentTextView.visibility = View.GONE
            stickerAnimView.visibility = View.GONE
            timeTextView.visibility = View.GONE
            dateTextView.visibility = View.GONE

            avatarImgView?.setImageDrawable(null)
            avatarImgView?.visibility = View.GONE
            typingAnimView?.visibility = View.GONE

            (stickerAnimView.drawable as LottieDrawable?)?.clearComposition()
        }
    }

    private fun getPreviousNotTypingMessage(position: Int): Message? {
        var i = position + 1
        while (i < currentList.lastIndex) {
            if (currentList[i].type != Message.TYPE_TYPING) {
                return currentList[i]
            }
            i++
        }
        return null
    }

    private fun getNextNotTypingMessage(position: Int): Message? {
        var i = position - 1
        while (i >= 0) {
            if (currentList[i].type != Message.TYPE_TYPING) {
                return currentList[i]
            }
            i--
        }
        return null
    }

    // view holder classes

    abstract inner class MessageViewHolder(itemView: View) : BindableViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val stickerAnimView: LottieAnimationView = itemView.findViewById(R.id.stickerAnimView)

        val avatarImgView: ImageView? = itemView.findViewById(R.id.avatarImgView)
        val typingAnimView: LottieAnimationView? = itemView.findViewById(R.id.typingAnimView)

        fun bindTime(curMessage: Message, nextMessage: Message?): Boolean {
            /* if one of these is true:
            - current curMessage is last curMessage
            - next curMessage time - current curMessage time < 1 min
            - next curMessage date != current curMessage date
            => display curMessage time
            */
            if (isBindTime(curMessage, nextMessage)) {
                timeTextView.text = timeFormat.format(curMessage.createdTime!!.toDate())
                timeTextView.visibility = View.VISIBLE
                return true
            }
            return false
        }

        fun bindDate(curMessage: Message, prevMessage: Message?) {
            /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
            if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
                dateTextView.text = dateFormat.format(curMessage.createdTime!!.toDate())
                dateTextView.visibility = View.VISIBLE
            }
        }

        fun bindSticker(message: Message) {
            stickerAnimView.visibility = View.VISIBLE
            LottieCompositionFactory.fromUrl(roomActivity, message.content).addListener { lottieComposition ->
                stickerAnimView.apply {
                    post {
                        setComposition(lottieComposition)
                    }
                }
            }
        }

        fun bindText(message: Message) {
            contentTextView.text = message.content
            contentTextView.visibility = View.VISIBLE
        }

        fun isBindTime(curMessage: Message, nextMessage: Message?): Boolean {
            return nextMessage == null ||
                    Utils.getTimeDiffInMillis(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                    Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())
        }
    }

    inner class SendViewHolder(itemView: View) : MessageViewHolder(itemView) {
        override fun bind(position: Int) {
            val curMessage = currentList[position]
            val nextMessage = getNextNotTypingMessage(position)
            val prevMessage = getPreviousNotTypingMessage(position)

            when (curMessage.type) {
                Message.TYPE_STICKER -> bindSticker(curMessage)
                else -> bindText(curMessage)
            }

            bindDate(curMessage, prevMessage)
            bindTime(curMessage, nextMessage)
        }
    }

    inner class RecvViewHolder(itemView: View) : MessageViewHolder(itemView) {
        override fun bind(position: Int) {
            val curMessage = currentList[position]
            val nextMessage = getNextNotTypingMessage(position)
            val prevMessage = getPreviousNotTypingMessage(position)

            if (curMessage.type == Message.TYPE_TYPING) {
                bindTyping()
                showAvatar(curMessage)
            } else {
                when (curMessage.type) {
                    Message.TYPE_STICKER -> bindSticker(curMessage)
                    else -> bindText(curMessage)
                }

                bindDate(curMessage, prevMessage)

                val isTimeBind = bindTime(curMessage, nextMessage)
                bindAvatar(curMessage, nextMessage, isTimeBind)
            }
        }

        private fun bindTyping() {
            typingAnimView!!.visibility = View.VISIBLE
        }

        fun bindAvatar(curMessage: Message, nextMessage: Message?, isTimeBind: Boolean) {
            /* if one of these is true:
            - curMessage time displayed
            - next curMessage sender != current curMessage sender
            => display avatarUrl
            */
            if (isTimeBind || nextMessage!!.senderPhone != curMessage.senderPhone) {
                showAvatar(curMessage)
            }
        }

        private fun showAvatar(message: Message) {
            avatarImgView!!.visibility = View.VISIBLE
            Picasso.get()
                    .load(message.senderAvatarUrl)
                    .fit()
                    .error(R.drawable.default_peer_avatar)
                    .into(avatarImgView)
        }
    }

    companion object {
        const val VIEW_TYPE_SEND = 0
        const val VIEW_TYPE_RECEIVE = 1
    }
}