package vng.zalo.tdtai.zalo.adapters

import android.media.ThumbnailUtils
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.squareup.picasso.Picasso
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ExternalSourceManager
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.factories.CacheDataSourceFactory
import vng.zalo.tdtai.zalo.models.message.*
import vng.zalo.tdtai.zalo.utils.MessageDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.views.activities.RoomActivity


class RoomActivityAdapter(private val roomActivity: RoomActivity, diffCallback: MessageDiffCallback) : ZaloListAdapter<Message, RoomActivityAdapter.MessageViewHolder>(diffCallback) {
    private val cacheDataSourceFactory = CacheDataSourceFactory(roomActivity, 100 * 1024 * 1024, 10 * 1024 * 1024)
    private val extractorsFactory = DefaultExtractorsFactory()
    var exoPlayer: ExoPlayer
    var lastTouchedVideoMessage: VideoMessage? = null

    init {
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter.Builder(roomActivity).build()
        val trackSelector: TrackSelector = DefaultTrackSelector(roomActivity)

        exoPlayer = SimpleExoPlayer.Builder(roomActivity)
                .setBandwidthMeter(bandwidthMeter)
                .setTrackSelector(trackSelector)
                .build()

        exoPlayer.playWhenReady = true
    }

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
            payloads: MutableList<Any?>
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
                    Message.PAYLOAD_UPLOAD_PROGRESS -> (holder as SendViewHolder).bindUploadProgress(curMessage as ResourceMessage)
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
//        holder.playerView.onResume()
//        holder.exoPlayer?.playWhenReady = true
    }

//    override fun onViewDetachedFromWindow(holder: MessageViewHolder) {
//        holder.exoPlayer?.playWhenReady = false
//        holder.playerView.onPause()
//    }

    override fun onViewRecycled(holder: MessageViewHolder) {
        holder.apply {
            contentTextView.visibility = View.GONE
            timeTextView.visibility = View.GONE
            dateTextView.visibility = View.GONE
            fileViewLayout.visibility = View.GONE
            callLayout.visibility = View.GONE
            paddingView.visibility = View.GONE

            cardView.visibility = View.GONE
            exoPlayer.release()
            mediaSource = null
            videoThumbImgView.setImageDrawable(null)
            isPreparing = false

            stickerAnimView.visibility = View.GONE
            (stickerAnimView.drawable as LottieDrawable?)?.clearComposition()

            imageView.visibility = View.GONE
            imageView.setImageDrawable(null)
            uploadImageProgressBar?.visibility = View.GONE
            uploadVideoProgressBar?.visibility = View.GONE
            uploadFileProgressBar?.visibility = View.GONE

            avatarImgView?.setImageDrawable(null)
            avatarImgView?.visibility = View.GONE
            typingAnimView?.visibility = View.GONE
        }
    }

    private fun getPreviousNotTypingMessage(position: Int): Message? {
        var i = position + 1
        while (i <= currentList.lastIndex) {
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
        val paddingView: View = itemView.findViewById(R.id.paddingView)

        // video view
        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        val videoThumbImgView: ImageView = itemView.findViewById(R.id.videoThumbImgView)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val videoDurationTV: TextView = itemView.findViewById(R.id.videoDurationTV)
        var mediaSource: MediaSource? = null
        var isPreparing = false

        //file layout
        val fileViewLayout: View = itemView.findViewById(R.id.fileViewLayout)
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        private val fileExtensionImgView: ImageView = itemView.findViewById(R.id.fileExtensionImgView)
        private val fileDescTextView: TextView = itemView.findViewById(R.id.fileDescTextView)
        val downloadFileImgView: View = itemView.findViewById(R.id.downloadFileImgView)
        val openFileTextView: View = itemView.findViewById(R.id.openFileTextView)

        //call layout
        val callLayout: View = itemView.findViewById(R.id.callLayout)
        private val callTitleTV: TextView = itemView.findViewById(R.id.callTitleTV)
        private val callTimeTV: TextView = itemView.findViewById(R.id.callTimeTV)
        private val callIconImgView: ImageView = itemView.findViewById(R.id.callIconImgView)
        private val callbackTV: TextView = itemView.findViewById(R.id.callbackTV)

        //upload progress bar
        val uploadImageProgressBar: ProgressBar? = itemView.findViewById(R.id.uploadImageProgressBar)
        val uploadVideoProgressBar: ProgressBar? = itemView.findViewById(R.id.uploadVideoProgressBar)
        val uploadFileProgressBar: ProgressBar? = itemView.findViewById(R.id.uploadFileProgressBar)

        val avatarImgView: ImageView? = itemView.findViewById(R.id.avatarImgView)
        val typingAnimView: LottieAnimationView? = itemView.findViewById(R.id.typingAnimView)

        fun bindTime(curMessage: Message, nextMessage: Message?): Boolean {
            /* if one of these is true:
            - current curMessage is last curMessage
            - next curMessage time - current curMessage time < 1 min
            - next curMessage date != current curMessage date
            => display curMessage time
            */
            if (shouldBindTime(curMessage, nextMessage)) {
                timeTextView.text = Utils.getTimeFormat(curMessage.createdTime!!.toDate())
                timeTextView.visibility = View.VISIBLE
                return true
            }
            timeTextView.visibility = View.GONE
            return false
        }

        fun bindDate(curMessage: Message, prevMessage: Message?) {
            /* if one of these is true:
                - current message is first message
                - previous message date != current message date
                => display date
                */
            if (prevMessage == null || Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), prevMessage.createdTime!!.toDate())) {
                dateTextView.text = Utils.getDateFormat(curMessage.createdTime!!.toDate())
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
                    Utils.areInDifferentDay(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate()) ||
                    Utils.areInDifferentMin(curMessage.createdTime!!.toDate(), nextMessage.createdTime!!.toDate())
        }

        fun bindImage(imageMessage: ImageMessage) {
            setViewConstrainRatio(imageView, imageMessage.ratio!!)

            imageView.visibility = View.VISIBLE

            Picasso.get().load(
                    if (Utils.isNetworkUri(imageMessage.url) || Utils.isContentUri(imageMessage.url)) {
                        imageMessage.url
                    } else {
                        "file://${imageMessage.url}"
                    }
            ).fit()
                    .error(R.drawable.load_image_fail)
                    .into(imageView)

            imageView.setOnClickListener(roomActivity)

            if (this is SendViewHolder) {
                bindUploadProgress(imageMessage)
            }
        }

        fun bindFile(fileMessage: FileMessage) {
            fileViewLayout.visibility = View.VISIBLE

            fileNameTextView.text = fileMessage.fileName

            val extension = Utils.getFileExtension(fileMessage.fileName)
            fileExtensionImgView.setImageResource(Utils.getResIdFromFileExtension(roomActivity, extension))

            val fileSizeFormat = if (fileMessage.size != -1L) Utils.getFormatFileSize(fileMessage.size) else ""

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

        fun bindVideo(videoMessage: VideoMessage) {
            itemView.apply {
                setViewConstrainRatio(cardView, videoMessage.ratio!!)

                cardView.visibility = View.VISIBLE

                videoDurationTV.text = Utils.getVideoDurationFormat(videoMessage.duration)

                playerView.controllerAutoShow = false

                mediaSource = LoopingMediaSource(
                        ProgressiveMediaSource.Factory(
                                cacheDataSourceFactory,
                                extractorsFactory
                        ).createMediaSource(Uri.parse(videoMessage.url))
                )

                val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter.Builder(roomActivity).build()
                val trackSelector: TrackSelector = DefaultTrackSelector(roomActivity)

                val exoPlayer = SimpleExoPlayer.Builder(roomActivity)
                        .setBandwidthMeter(bandwidthMeter)
                        .setTrackSelector(trackSelector)
                        .build()

                exoPlayer.playWhenReady = true
                exoPlayer.addListener(object :Player.EventListener{
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        if(isPreparing && playbackState == ExoPlayer.STATE_READY){
                            videoThumbImgView.visibility = View.GONE
                            isPreparing = false
                        }
                    }
                })

                cardView.setOnTouchListener { _, _ ->
                    if (lastTouchedVideoMessage !== videoMessage) {
                        playerView.player = exoPlayer
                        exoPlayer.prepare(mediaSource!!)
                        isPreparing = true
                        lastTouchedVideoMessage = videoMessage
                    }
                    false
                }

                ExternalSourceManager.getVideoThumbBitmap(context, videoMessage.url){
                    videoThumbImgView.setImageBitmap(it)
                }
            }
        }

        private fun setViewConstrainRatio(view: View, ratio: String) {
            val dimension = Utils.getDimension(ratio)

            val constraintLayout = view.parent as ConstraintLayout

            val set = ConstraintSet()
            set.clone(constraintLayout)
            set.constrainMaxWidth(view.id, dimension.first)
            set.constrainMaxHeight(view.id, dimension.second)
            set.setDimensionRatio(view.id, ratio)
            set.applyTo(constraintLayout)
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
                Message.TYPE_VIDEO -> bindVideo(curMessage as VideoMessage)
                else -> bindText(curMessage as TextMessage)
            }

            bindDate(curMessage, prevMessage)
            bindTime(curMessage, nextMessage)
            bindPadding(curMessage, prevMessage)
            if(curMessage is ResourceMessage){
                bindUploadProgress(curMessage)
            }

            itemView.setOnLongClickListener(roomActivity)
        }

        fun bindUploadProgress(resourceMessage: ResourceMessage) {
            val uploadProgressBar = when (resourceMessage.type) {
                Message.TYPE_IMAGE -> uploadImageProgressBar
                Message.TYPE_VIDEO -> uploadVideoProgressBar
                else -> uploadFileProgressBar
            }
            if (resourceMessage.uploadProgress == null) {
                uploadProgressBar!!.visibility = View.GONE
                if (resourceMessage.type == Message.TYPE_FILE) {
                    downloadFileImgView.visibility = View.VISIBLE
                }
            } else {
                uploadProgressBar!!.progress = resourceMessage.uploadProgress!!
                uploadProgressBar.visibility = View.VISIBLE
                if (resourceMessage.type == Message.TYPE_FILE) {
                    downloadFileImgView.visibility = View.GONE
                }
            }
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
                    Message.TYPE_VIDEO -> bindVideo(curMessage as VideoMessage)
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
            } else {
                avatarImgView!!.visibility = View.GONE
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