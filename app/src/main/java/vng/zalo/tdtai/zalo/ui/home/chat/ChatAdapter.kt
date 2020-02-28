package vng.zalo.tdtai.zalo.ui.home.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_receive_room_activity.view.*
import kotlinx.android.synthetic.main.item_seen_room_activity.view.*
import kotlinx.android.synthetic.main.item_send_room_activity.view.*
import kotlinx.android.synthetic.main.part_call_message.view.*
import kotlinx.android.synthetic.main.part_chat_date.view.*
import kotlinx.android.synthetic.main.part_chat_padding.view.*
import kotlinx.android.synthetic.main.part_chat_time.view.*
import kotlinx.android.synthetic.main.part_file_message.view.*
import kotlinx.android.synthetic.main.part_image_message.view.*
import kotlinx.android.synthetic.main.part_sticker_message.view.*
import kotlinx.android.synthetic.main.part_text_message.view.*
import kotlinx.android.synthetic.main.part_video_message.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.ui.chat.RoomMemberAdapter
import vng.zalo.tdtai.zalo.model.message.*
import vng.zalo.tdtai.zalo.utils.MessageDiffCallback
import vng.zalo.tdtai.zalo.utils.RoomMemberDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.utils.loadCompat
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import java.util.*
import javax.inject.Inject


class ChatAdapter @Inject constructor(
        private val chatActivity: ChatActivity,
        diffCallback: MessageDiffCallback,
        private val sessionManager: SessionManager,
        private val utils: Utils,
        private val resourceManager: ResourceManager
) : ZaloListAdapter<Message, BindableViewHolder>(diffCallback) {
    private val roomEnterTime = System.currentTimeMillis()

    var exoPlayer: ExoPlayer

    init {
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter.Builder(chatActivity).build()
        val trackSelector: TrackSelector = DefaultTrackSelector(chatActivity)

        exoPlayer = SimpleExoPlayer.Builder(chatActivity)
                .setBandwidthMeter(bandwidthMeter)
                .setTrackSelector(trackSelector)
                .build()

        exoPlayer.playWhenReady = true
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            currentList[position].type == Message.TYPE_SEEN -> VIEW_TYPE_SEEN
            currentList[position].senderPhone == sessionManager.curUser!!.phone -> VIEW_TYPE_SEND
            else -> VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        val holder = when (viewType) {
            VIEW_TYPE_SEND -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_send_room_activity, parent, false)
                SendViewHolder(v)
            }
            VIEW_TYPE_RECEIVE -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_receive_room_activity, parent, false)
                RecvViewHolder(v)
            }
            else -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_seen_room_activity, parent, false)
                SeenViewHolder(v)
            }
        }

        return holder.apply {
            if (this is MessageViewHolder) {
                itemView.apply {
                    imageView.setOnClickListener(chatActivity)
                    downloadFileImgView.setOnClickListener(chatActivity)
                    callbackTV.setOnClickListener(chatActivity)
                    videoMessageLayout.setOnClickListener(chatActivity)

                    setOnLongClickListener(chatActivity)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onBindViewHolder(
            holder: BindableViewHolder,
            position: Int,
            payloads: MutableList<Any?>
    ) {
        try {
            if (payloads.isNotEmpty()) {
                val curMessage = currentList[position]
                val nextMessage = getNextRealMessage(position)
//            val prevMessage = getPreviousNotTypingMessage(position)
                (payloads[0] as ArrayList<*>).forEach {
                    when (it) {
                        Message.PAYLOAD_SEEN -> (holder as SeenViewHolder).bindSeenMembers(curMessage as SeenMessage)
                        else -> {
                            holder as MessageViewHolder
                            when (it) {
                                Message.PAYLOAD_TIME -> holder.bindTime(curMessage, nextMessage)
                                Message.PAYLOAD_AVATAR -> {
                                    when (holder.itemViewType) {
                                        VIEW_TYPE_RECEIVE -> (holder as RecvViewHolder).apply {
                                            bindAvatar(curMessage, nextMessage, shouldBindTime(curMessage, nextMessage))
                                        }
                                    }
                                }
                                Message.PAYLOAD_UPLOAD_PROGRESS -> (holder as SendViewHolder).bindUploadProgress(curMessage as ResourceMessage)
                                Message.PAYLOAD_SEND_STATUS -> (holder as SendViewHolder).bindSendStatus(curMessage, nextMessage)
                            }
                        }
                    }
                }
            } else {
                onBindViewHolder(holder, position)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun onViewRecycled(holder: BindableViewHolder) {
        if (holder is MessageViewHolder) {
            holder.apply {
                itemView.apply {
                    textMessageLayout.visibility = View.GONE
                    timeLayout.visibility = View.GONE
                    sentImgView.visibility = View.GONE
                    dateTextView.visibility = View.GONE
                    fileMessageLayout.visibility = View.GONE
                    callMessageLayout.visibility = View.GONE
                    paddingView.visibility = View.GONE

                    videoMessageLayout.visibility = View.GONE
                    videoThumbImgView.setImageDrawable(null)
                    stickerAnimView.visibility = View.GONE
                    (stickerAnimView.drawable as LottieDrawable?)?.clearComposition()

                    imageView.visibility = View.GONE
                    imageView.setImageDrawable(null)

                    if(holder is SendViewHolder){
                        uploadImageProgressBar?.visibility = View.GONE
                        uploadVideoProgressBar?.visibility = View.GONE
                        uploadFileProgressBar?.visibility = View.GONE
                    }else{
                        avatarLayout?.visibility = View.GONE
                        avatarImgView?.setImageDrawable(null)
                        typingMessageLayout?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun getPreviousRealMessage(position: Int): Message? {
        var i = position + 1
        while (i <= currentList.lastIndex) {
            if (currentList[i].type != Message.TYPE_TYPING && currentList[i].type != Message.TYPE_SEEN) {
                return currentList[i]
            }
            i++
        }
        return null
    }

    private fun getNextRealMessage(position: Int): Message? {
        var i = position - 1
        while (i >= 0) {
            if (currentList[i].type != Message.TYPE_TYPING && currentList[i].type != Message.TYPE_SEEN) {
                return currentList[i]
            }
            i--
        }
        return null
    }

    // view holder classes

    abstract inner class MessageViewHolder(itemView: View) : BindableViewHolder(itemView) {
        fun bindTime(curMessage: Message, nextMessage: Message?): Boolean {
            /* if one of these is true:
            - current curMessage is last curMessage
            - next curMessage time - current curMessage time < 1 min
            - next curMessage date != current curMessage date
            => display curMessage time
            */
            if (shouldBindTime(curMessage, nextMessage)) {
                itemView.apply {
                    timeTextView.text = utils.getTimeFormat(curMessage.createdTime!!)
                    timeLayout.visibility = View.VISIBLE
                }
//                itemView as ConstraintLayout
//
//                val timeLayout = View.inflate(itemView.context, R.layout.part_chat_time, null)
//
//                timeLayout.timeTextView.text = Utils.getTimeFormat(curMessage.createdTime!!.toDate())
//                itemView.addView(timeLayout, 0)
//
//                ConstraintSet().apply {
//                    clone(itemView)
//                    connect(itemView.imageView.id, ConstraintSet.BOTTOM, timeLayout.id, ConstraintSet.TOP, 4)
//                    connect(timeLayout.id, ConstraintSet.TOP, itemView.imageView.id, ConstraintSet.BOTTOM, 4)
//                    connect(timeLayout.id, ConstraintSet.BOTTOM, itemView.id, ConstraintSet.BOTTOM, 16)
//                    connect(timeLayout.id, ConstraintSet.END, itemView.id, ConstraintSet.END, 24)
//                    applyTo(itemView)
//                }

                return true
            }else if(itemView.timeLayout.visibility != View.GONE){
                    itemView.timeLayout.visibility = View.GONE
            }
            return false
        }

        fun bindDate(curMessage: Message, prevMessage: Message?) {
            /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
            if (prevMessage == null || utils.areInDifferentDay(curMessage.createdTime!!, prevMessage.createdTime!!)) {
                itemView.apply {
                    dateTextView.text = utils.getDateFormat(curMessage.createdTime!!)
                    dateTextView.visibility = View.VISIBLE
                }
            }
        }

        fun bindSticker(stickerMessage: StickerMessage) {
            itemView.apply {
                LottieCompositionFactory.fromUrl(chatActivity, stickerMessage.url).addListener {
                    stickerAnimView.setComposition(it)
                    if (stickerAnimView.isAttachedToWindow && chatActivity.isRecyclerViewIdle()) {
                        stickerAnimView.resumeAnimation()
                    }
                }
//            stickerMessageAnimView.setAnimationFromUrl(stickerMessage.url)
//            stickerAnimView.scaleType = ImageView.ScaleType.CENTER_CROP
                stickerAnimView.visibility = View.VISIBLE
            }
        }

        fun bindText(textMessage: TextMessage) {
            itemView.apply {
                bindBackgroundColor(textMessageTV)

                textMessageTV.text = textMessage.content
//            textMessageTV.movementMethod = LinkMovementMethod.getInstance()

                textMessageLayout.visibility = View.VISIBLE
            }
        }

        private fun bindBackgroundColor(view: View) {
            if (itemViewType == VIEW_TYPE_SEND) {
                view.setBackgroundResource(R.color.sendMessageBackground)
            } else {
                view.setBackgroundResource(R.color.recvMessageBackground)
            }
        }

        fun shouldBindTime(curMessage: Message, nextMessage: Message?): Boolean {
            return nextMessage == null ||
                    utils.areInDifferentDay(curMessage.createdTime!!, nextMessage.createdTime!!) ||
                    utils.areInDifferentMin(curMessage.createdTime!!, nextMessage.createdTime!!)
        }

        fun bindImage(imageMessage: ImageMessage) {
            itemView.apply {
                setViewConstrainRatio(imageView, imageMessage.ratio!!)

                imageView.visibility = View.VISIBLE

                Picasso.get().loadCompat(imageMessage.url, resourceManager)
                        .fit()
                        .error(R.drawable.load_image_fail)
                        .into(imageView)
            }
        }

        fun bindFile(fileMessage: FileMessage) {
            itemView.apply {
                bindBackgroundColor(fileMessageLayout2)

                fileMessageLayout.visibility = View.VISIBLE

                fileNameTextView.text = fileMessage.fileName
                        ?: chatActivity.getString(R.string.label_no_name)

                val extension = utils.getFileExtension(fileMessage.fileName).toUpperCase(Locale.US)
                fileExtensionImgView.setImageResource(utils.getResIdFromFileExtension(chatActivity, extension))

                val fileSizeFormat = if (fileMessage.size != -1L) utils.getFormatFileSize(fileMessage.size) else ""

                fileDescTextView.text =
                        if (extension != "" && fileSizeFormat != "") {
                            String.format("%s - %s", extension, fileSizeFormat)
                        } else if (extension != "") {
                            extension
                        } else {
                            fileSizeFormat
                        }
            }
        }

        fun bindCall(callMessage: CallMessage) {
            itemView.apply {
                bindBackgroundColor(callMessageLayout2)

                val context = itemView.context

                callMessageLayout.visibility = View.VISIBLE

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
                    callTimeTV.text = utils.getCallTimeFormat(context, callMessage.callTime)

                    if (callMessage.senderPhone == sessionManager.curUser!!.phone) {
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
            }
        }

        fun bindPadding(curMessage: Message, prevMessage: Message?) {
            itemView.apply {
                if (prevMessage != null && prevMessage.senderPhone != curMessage.senderPhone) {
                    paddingView.visibility = View.VISIBLE
                } else {
                    paddingView.visibility = View.GONE
                }
            }
        }

        fun bindVideo(videoMessage: VideoMessage) {
            itemView.apply {
                setViewConstrainRatio(videoMessageLayout, videoMessage.ratio!!)

                resourceManager.getVideoThumbUri(context, videoMessage.url) {
                    Picasso.get().loadCompat(it, resourceManager)
                            .fit()
                            .into(videoThumbImgView)
                }

                videoMessageLayout.visibility = View.VISIBLE

                videoDurationTV.text = utils.getVideoDurationFormat(videoMessage.duration)
            }
        }

        private fun setViewConstrainRatio(view: View, ratio: String) {
            val dimension = utils.getDimension(ratio)

            val constraintLayout = view.parent as ConstraintLayout

            val set = ConstraintSet()
            set.clone(constraintLayout)
            set.constrainMaxWidth(view.id, dimension.first)
            set.constrainMaxHeight(view.id, dimension.second)
            set.setDimensionRatio(view.id, "H,$ratio")
            set.applyTo(constraintLayout)
        }
    }

    inner class SendViewHolder(itemView: View) : MessageViewHolder(itemView) {
//        //send status
//        val sentImgView: ImageView = itemView.findViewById(R.id.sentImgView)
//        //avatar layout
//        val avatarLayout: CardView? = itemView.findViewById(R.id.avatarLayout)
//        val avatarImgView: ImageView? = itemView.findViewById(R.id.avatarImgView)
//
//        val typingMessageLayout: CardView? = itemView.findViewById(R.id.typingMessageLayout)

        override fun bind(position: Int) {
            val curMessage = currentList[position]
            val nextMessage = getNextRealMessage(position)
            val prevMessage = getPreviousRealMessage(position)

            when (curMessage.type) {
                Message.TYPE_STICKER -> bindSticker(curMessage as StickerMessage)
                Message.TYPE_IMAGE -> bindImage(curMessage as ImageMessage)
                Message.TYPE_FILE -> bindFile(curMessage as FileMessage)
                Message.TYPE_CALL -> bindCall(curMessage as CallMessage)
                Message.TYPE_VIDEO -> bindVideo(curMessage as VideoMessage)
                else -> bindText(curMessage as TextMessage)
            }

            bindDate(curMessage, prevMessage)
            bindTime(curMessage, nextMessage)
            bindPadding(curMessage, prevMessage)
            if (curMessage is ResourceMessage) {
                bindUploadProgress(curMessage)
            }

            //this is newest real message
            if (curMessage.createdTime!! > roomEnterTime) {
                bindSendStatus(curMessage, nextMessage)
            }
        }

        fun bindUploadProgress(resourceMessage: ResourceMessage) {
            itemView.apply {
                val uploadProgressBar = when (resourceMessage.type) {
                    Message.TYPE_IMAGE -> uploadImageProgressBar
                    Message.TYPE_VIDEO -> uploadVideoProgressBar
                    else -> uploadFileProgressBar
                }
                if (resourceMessage.uploadProgress == null) {
                    uploadProgressBar!!.visibility = View.GONE
                    if (resourceMessage.type == Message.TYPE_FILE) {
                        itemView.downloadFileImgView.visibility = View.VISIBLE
                    }
                } else {
                    uploadProgressBar!!.progress = resourceMessage.uploadProgress!!
                    uploadProgressBar.visibility = View.VISIBLE
                    if (resourceMessage.type == Message.TYPE_FILE) {
                        itemView.downloadFileImgView.visibility = View.GONE
                    }
                }
            }
        }

        fun bindSendStatus(curMessage: Message, nextMessage: Message?) {
            if (shouldBindTime(curMessage, nextMessage)) {
                itemView.sentImgView.visibility = if (curMessage.isSent) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    inner class RecvViewHolder(itemView: View) : MessageViewHolder(itemView) {
        override fun bind(position: Int) {
            val curMessage = currentList[position]
            val nextMessage = getNextRealMessage(position)
            val prevMessage = getPreviousRealMessage(position)

            if (curMessage.type == Message.TYPE_TYPING) {
                bindTyping()
                showAvatar(curMessage)
            } else {
                when (curMessage.type) {
                    Message.TYPE_STICKER -> bindSticker(curMessage as StickerMessage)
                    Message.TYPE_IMAGE -> bindImage(curMessage as ImageMessage)
                    Message.TYPE_FILE -> bindFile(curMessage as FileMessage)
                    Message.TYPE_CALL -> bindCall(curMessage as CallMessage)
                    Message.TYPE_VIDEO -> bindVideo(curMessage as VideoMessage)
                    else -> bindText(curMessage as TextMessage)
                }

                bindDate(curMessage, prevMessage)
                bindPadding(curMessage, prevMessage)

                val isTimeBind = bindTime(curMessage, nextMessage)
                bindAvatar(curMessage, nextMessage, isTimeBind)
            }
        }

        private fun bindTyping() {
            itemView.typingMessageLayout.visibility = View.VISIBLE
        }

        fun bindAvatar(curMessage: Message, nextMessage: Message?, isTimeBind: Boolean) {
            /* if one of these is true:
            - curMessage time displayed
            - next curMessage sender != current curMessage sender
            => display avatarUrl
            */
            if (isTimeBind || nextMessage!!.senderPhone != curMessage.senderPhone) {
                showAvatar(curMessage)
            } else {
                itemView.avatarLayout.visibility = View.GONE
            }
        }

        private fun showAvatar(message: Message) {
            itemView.apply {
                avatarLayout.visibility = View.VISIBLE
                Picasso.get()
                        .loadCompat(message.senderAvatarUrl, resourceManager)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.default_peer_avatar)
                        .into(avatarImgView)
            }
        }
    }

    inner class SeenViewHolder(itemView: View) : BindableViewHolder(itemView) {
        val adapter = RoomMemberAdapter(RoomMemberDiffCallback(), resourceManager)

        override fun bind(position: Int) {
            itemView.seenRecyclerView.adapter = adapter

            val message = currentList[position] as SeenMessage

            bindSeenMembers(message)
        }

        fun bindSeenMembers(message: SeenMessage) {
            adapter.submitList(message.seenMembers)
        }
    }

    companion object {
        const val VIEW_TYPE_SEND = 0
        const val VIEW_TYPE_RECEIVE = 1
        const val VIEW_TYPE_SEEN = 2
    }
}