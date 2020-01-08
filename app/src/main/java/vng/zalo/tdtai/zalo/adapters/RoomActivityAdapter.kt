package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.squareup.picasso.Picasso
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.models.message.*
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.MessageDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.views.activities.RoomActivity
import java.text.DateFormat


class RoomActivityAdapter(private val roomActivity: RoomActivity, diffCallback: MessageDiffCallback) : ListAdapter<Message, RoomActivityAdapter.MessageViewHolder>(diffCallback) {
    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].senderPhone) {
            ZaloApplication.curUser!!.phone -> VIEW_TYPE_SEND
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
                                bindAvatar(curMessage, nextMessage, shouldBindTime(curMessage, nextMessage))
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
            timeTextView.visibility = View.GONE
            dateTextView.visibility = View.GONE
            fileViewLayout.visibility = View.GONE
            callLayout.visibility = View.GONE

            stickerAnimView.visibility = View.GONE
            (stickerAnimView.drawable as LottieDrawable?)?.clearComposition()

            imageView.visibility = View.GONE
            imageView.setImageDrawable(null)

            avatarImgView?.setImageDrawable(null)
            avatarImgView?.visibility = View.GONE
            typingAnimView?.visibility = View.GONE
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
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val paddingView: View = itemView.findViewById(R.id.paddingView)

        //file layout
        val fileViewLayout: View = itemView.findViewById(R.id.fileViewLayout)
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        private val fileExtensionImgView: ImageView = itemView.findViewById(R.id.fileExtensionImgView)
        private val fileDescTextView: TextView = itemView.findViewById(R.id.fileDescTextView)
        private val downloadFileImgView: View = itemView.findViewById(R.id.downloadImgView)
        private val openFileImgView: View = itemView.findViewById(R.id.openImgView)

        //call layout
        val callLayout: View = itemView.findViewById(R.id.callLayout)
        private val callTitleTV: TextView = itemView.findViewById(R.id.callTitleTV)
        private val callTimeTV: TextView = itemView.findViewById(R.id.callTimeTV)
        private val callIconImgView: ImageView = itemView.findViewById(R.id.callIconImgView)
        private val callbackTV: TextView = itemView.findViewById(R.id.callbackTV)

        val avatarImgView: ImageView? = itemView.findViewById(R.id.avatarImgView)
        val typingAnimView: LottieAnimationView? = itemView.findViewById(R.id.typingAnimView)

        private val dateFormat = java.text.SimpleDateFormat.getDateInstance()
        private val timeFormat = java.text.SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

        fun bindTime(curMessage: Message, nextMessage: Message?): Boolean {
            /* if one of these is true:
            - current curMessage is last curMessage
            - next curMessage time - current curMessage time < 1 min
            - next curMessage date != current curMessage date
            => display curMessage time
            */
            if (shouldBindTime(curMessage, nextMessage)) {
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

        fun bindSticker(stickerMessage: StickerMessage) {
            stickerAnimView.setAnimationFromUrl(stickerMessage.url)
//            stickerAnimView.scaleType = ImageView.ScaleType.CENTER_CROP
            stickerAnimView.visibility = View.VISIBLE
        }

        fun bindText(textMessage: TextMessage) {
            contentTextView.text = textMessage.content
            contentTextView.visibility = View.VISIBLE
        }

        fun shouldBindTime(curMessage: Message, nextMessage: Message?): Boolean {
            return nextMessage == null ||
                    Utils.getTimeDiffInMillis(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                    Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())
        }

        fun bindImage(imageMessage: ImageMessage) {
            val dimension = Utils.getDimension(imageMessage.ratio!!)

            //set aspect ratio
            val set = ConstraintSet()
            set.clone(itemView as ConstraintLayout)
            set.constrainMaxWidth(imageView.id, dimension.first)
            set.constrainMaxHeight(imageView.id, dimension.second)
            set.setDimensionRatio(imageView.id, imageMessage.ratio)
            set.applyTo(itemView)

            imageView.visibility = View.VISIBLE

            Picasso.get().load(imageMessage.url)
                    .fit()
                    .error(R.drawable.load_image_fail)
                    .into(imageView)

            imageView.setOnClickListener(roomActivity)
        }

        fun bindFile(fileMessage: FileMessage) {
            fileViewLayout.visibility = View.VISIBLE

            fileNameTextView.text = fileMessage.fileName

            val extension = Utils.getFileExtensionFromFileName(fileMessage.fileName!!)
            fileExtensionImgView.setImageResource(Utils.getResIdFromFileExtension(roomActivity, extension))

            val fileSizeFormat = if (fileMessage.fileSize != -1L) Utils.getFormatFileSize(fileMessage.fileSize!!) else ""

            fileDescTextView.text =
                    if (extension != "" && fileSizeFormat != "") {
                        String.format("%s - %s", extension, fileSizeFormat)
                    } else if (extension != "") {
                        extension
                    } else {
                        fileSizeFormat
                    }

            downloadFileImgView.setOnClickListener(roomActivity)
        }

        fun bindCall(callMessage: CallMessage) {
            val context = itemView.context

            callLayout.visibility = View.VISIBLE

            if (callMessage.isMissed) {
                callTitleTV.setTextColor(ContextCompat.getColor(context, R.color.missedCall))
                callTitleTV.text = context.getString(R.string.description_missed_call)

                callTimeTV.text = context.getString(
                        if (callMessage.callType == CallMessage.CALL_TYPE_VOICE)
                            R.string.description_voice_call
                        else
                            R.string.description_video_call
                )

                callIconImgView.setImageResource(R.drawable.missed_call)
            } else {
                callTimeTV.text = Utils.getCallTimeFormat(context, callMessage.callTime)

                if (callMessage.senderPhone == ZaloApplication.curUser!!.phone) {
                    callTitleTV.text = context.getString(
                            if (callMessage.callType == CallMessage.CALL_TYPE_VOICE) {
                                R.string.description_outgoing_voice_call
                            } else {
                                R.string.description_outgoing_video_call
                            }
                    )

                    callIconImgView.setImageResource(
                            if (callMessage.isCanceled) {
                                R.drawable.canceled_outgoing_call
                            } else {
                                R.drawable.success_outgoing_call
                            }
                    )
                } else {
                    callTitleTV.text = context.getString(
                            if (callMessage.callType == CallMessage.CALL_TYPE_VOICE) {
                                R.string.description_incoming_voice_call
                            } else {
                                R.string.description_incoming_video_call
                            }
                    )

                    callIconImgView.setImageResource(
                            if (callMessage.isCanceled) {
                                R.drawable.canceled_incoming_call
                            } else {
                                R.drawable.success_incoming_call
                            }
                    )
                }
            }

            callbackTV.setOnClickListener(roomActivity)
        }

        fun bindPadding(curMessage: Message, prevMessage: Message?) {
            if (prevMessage != null && prevMessage.senderPhone != curMessage.senderPhone) {
                paddingView.visibility = View.VISIBLE
            } else {
                paddingView.visibility = View.GONE
            }
        }
    }

    inner class SendViewHolder(itemView: View) : MessageViewHolder(itemView) {
        override fun bind(position: Int) {
            val curMessage = currentList[position]
            val nextMessage = getNextNotTypingMessage(position)
            val prevMessage = getPreviousNotTypingMessage(position)

            when (curMessage.type) {
                Message.TYPE_STICKER -> bindSticker(curMessage as StickerMessage)
                Message.TYPE_IMAGE -> bindImage(curMessage as ImageMessage)
                Message.TYPE_FILE -> bindFile(curMessage as FileMessage)
                Message.TYPE_CALL -> bindCall(curMessage as CallMessage)
                else -> bindText(curMessage as TextMessage)
            }

            bindDate(curMessage, prevMessage)
            bindTime(curMessage, nextMessage)
            bindPadding(curMessage, prevMessage)

            itemView.setOnLongClickListener(roomActivity)
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
                    Message.TYPE_STICKER -> bindSticker(curMessage as StickerMessage)
                    Message.TYPE_IMAGE -> bindImage(curMessage as ImageMessage)
                    Message.TYPE_FILE -> bindFile(curMessage as FileMessage)
                    Message.TYPE_CALL -> bindCall(curMessage as CallMessage)
                    else -> bindText(curMessage as TextMessage)
                }

                bindDate(curMessage, prevMessage)
                bindPadding(curMessage, prevMessage)

                val isTimeBind = bindTime(curMessage, nextMessage)
                bindAvatar(curMessage, nextMessage, isTimeBind)
            }

            itemView.setOnLongClickListener(roomActivity)
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
                    .centerInside()
                    .error(R.drawable.default_peer_avatar)
                    .into(avatarImgView)
        }
    }

    companion object {
        const val VIEW_TYPE_SEND = 0
        const val VIEW_TYPE_RECEIVE = 1
    }
}