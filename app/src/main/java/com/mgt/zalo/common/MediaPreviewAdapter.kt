package com.mgt.zalo.common

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.util.diff_callback.MediaDiffCallback
import com.mgt.zalo.util.smartLoad
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_media_video_playable_preview.view.*
import kotlinx.android.synthetic.main.part_media_preview.view.*
import kotlinx.android.synthetic.main.part_media_video_preview.view.*
import javax.inject.Inject

class MediaPreviewAdapter @Inject constructor(
        private val eventListener: BaseOnEventListener,
        diffCallback: MediaDiffCallback
) : BaseListAdapter<Media, MediaPreviewAdapter.MediaPreviewHolder>(diffCallback) {
    var moreCount = 0
    var doLooping = false

    override fun getItemViewType(position: Int): Int {
        return if (currentList[position] is VideoMedia) {
            if(itemCount == 1){
                TYPE_VIDEO_PLAYABLE
            }else{
                TYPE_VIDEO_NOT_PLAYABLE
            }
        } else {
            TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaPreviewHolder {
        return when (viewType) {
            TYPE_IMAGE -> ImageMediaPreviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_image_preview, parent, false))
            TYPE_VIDEO_NOT_PLAYABLE -> VideoMediaPreviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_video_preview, parent, false))
            else -> VideoMediaPlayableHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_video_playable_preview, parent, false))
        }.apply { bindOneTime() }
    }

    override fun onBindViewHolder(holder: MediaPreviewHolder, position: Int, payloads: ArrayList<*>) {
        payloads.forEach {
            when (it) {
                Media.PAYLOAD_DISPLAY_MORE -> holder.bindDisplayMore(position)
                Media.PAYLOAD_RATIO -> holder.bindRatio()
            }
        }
    }

    override fun onBindViewHolder(holder: MediaPreviewHolder, position: Int) {
        holder.bind(position)
    }

    abstract inner class MediaPreviewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            bindRatio()
            itemView.imageView.visibility = View.VISIBLE
            bindDisplayMore(position)
        }

        fun bindRatio() {
            itemView.apply {
                imageView.layoutParams = imageView.layoutParams.apply {
                    if (itemCount == 1) {
                        val media = currentList[0]

                        this as ConstraintLayout.LayoutParams
                        dimensionRatio = utils.getAdjustedRatio(media.ratio!!)
                        height = 0
                    } else {
                        height = ConstraintLayout.LayoutParams.MATCH_PARENT
                    }
                }
            }
        }

        fun bindDisplayMore(position: Int) {
            itemView.apply {
                displayMoreTextView.visibility = if (moreCount > 0 && position == currentList.lastIndex) {
                    displayMoreTextView.text = "+$moreCount"
                    imageView.foreground = ColorDrawable(ContextCompat.getColor(context, R.color.blackTransparent))
                    View.VISIBLE
                } else {
                    imageView.foreground = null
                    View.GONE
                }
            }
        }

        open fun bindOneTime() {
            itemView.setOnClickListener(eventListener)
        }
    }

    open inner class VideoMediaPreviewHolder(itemView: View) : MediaPreviewHolder(itemView) {
        override fun bind(position: Int) {
            super.bind(position)

            val videoMedia = currentList[position] as VideoMedia

            itemView.apply {
                resourceManager.getVideoThumbUri(videoMedia.uri!!) { uri ->
                    Picasso.get().smartLoad(uri, resourceManager, imageView) {
                        it.fit().centerCrop()
                    }
                }

                videoDurationTV.text = utils.getVideoDurationFormat(videoMedia.duration)
            }
        }
    }

    fun onLostFocus(holder:RecyclerView.ViewHolder){
        if(holder is VideoMediaPlayableHolder){
            playbackManager.pause()
            holder.itemView.apply {
                playImgView.visibility = View.VISIBLE
                playerView.player = null
                imageView.apply { visibility = View.VISIBLE }
            }
        }
    }

    fun onPause(holder:RecyclerView.ViewHolder){
        if(holder is VideoMediaPlayableHolder){
            holder.itemView.apply {
                playImgView.visibility = View.VISIBLE
                playbackManager.pause()
            }
        }
    }

    fun onResume(holder:RecyclerView.ViewHolder){
        if(holder is VideoMediaPlayableHolder){
            holder.itemView.apply {
                playImgView.visibility = View.GONE
            }
            playbackManager.resume()
        }
    }

    inner class VideoMediaPlayableHolder(itemView: View) : VideoMediaPreviewHolder(itemView) {
        fun playResumeVideo(videoMedia: VideoMedia) {
            val onLostFocusOrEnded = {
                onLostFocus(this)
            }

            itemView.apply {
                playImgView.visibility = View.GONE
                if(playerView.player == null){
                    playbackManager.prepare(videoMedia.uri!!, doLooping, onReady = {
                        imageView.apply { visibility = View.INVISIBLE}
                        onResume(this@VideoMediaPlayableHolder)
                    }, onLostFocus = onLostFocusOrEnded,
                            onEnded = onLostFocusOrEnded)
                    playerView.player = playbackManager.exoPlayer
                }else{
                    onResume(this@VideoMediaPlayableHolder)
                }
            }
        }

        override fun bindOneTime() {
            super.bindOneTime()
            itemView.playerView.controllerAutoShow = false
        }
    }

    inner class ImageMediaPreviewHolder(itemView: View) : MediaPreviewHolder(itemView) {
        override fun bind(position: Int) {
            super.bind(position)

            val media = currentList[position]
            itemView.apply {
                Picasso.get().smartLoad(media.uri, resourceManager, imageView) {
                    it.fit().centerCrop()
                }
            }
        }
    }

//    fun onFocused(recyclerView: RecyclerView) {
//        Log.d(TAG, "focus")
//        val curItemView = recyclerView.findViewHolderForAdapterPosition(curPosition)?.itemView
//        curItemView?.apply {
//            playbackManager.prepare()
//
//            playbackManager.prepare(currentList[watchFragment.getCurrentItem()].videoUrl!!, playerView, true, onReady)
//        }
//
////        watchFragment.isPaused = false
//    }
//
//    private fun resumeCurrentVideo(){
//
//    }
//
//    fun onWatchNotFocused(itemView: View) {
//        Log.d(TAG, "lost focus")
//        itemView.apply {
//            playerView.player = null
//
//            previewImgView.visibility = View.VISIBLE
//            playPauseImgView.visibility = View.GONE
//            musicNameTextView.isSelected = false
//            musicOwnerAvatarImgView.rotation = 0f
//        }
//    }
//
//    fun onWatchResume(itemView: View) {
//        Log.d(TAG, "resume")
//        itemView.apply {
//            playbackManager.play()
//            playPauseImgView.visibility = View.GONE
//            previewImgView.visibility = View.GONE
//            musicNameTextView.isSelected = true
//            if (isAnimatorStarted) {
//                objectAnimator.resume()
//            } else {
//                objectAnimator.start()
//                isAnimatorStarted = true
//            }
//        }
//    }
//
//    fun onWatchPause(itemView: View, displayPauseIcon: Boolean = true) {
//        Log.d(TAG, "pause")
//        itemView.apply {
//            playbackManager.pause()
//            if (displayPauseIcon) {
//                playPauseImgView.visibility = View.VISIBLE
//            }
//            musicNameTextView.isSelected = false
//            objectAnimator.pause()
//        }
//    }

    fun submitListLimit(list: List<Media>, callback: (() -> Unit)? = null) {
        var listToSubmit = list
        if (list.size > 5) {
            moreCount = list.size - 4
            listToSubmit = listToSubmit.take(LIMIT_NUM) as ArrayList
        } else {
            moreCount = 0
        }

        val needResetRatio = itemCount != listToSubmit.size
        submitList(listToSubmit) {
            if (needResetRatio) {
                notifyItemRangeChanged(0, itemCount, arrayListOf(Media.PAYLOAD_RATIO))
            }
            callback?.invoke()
        }
    }

    companion object {
        const val LIMIT_NUM = 5

        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO_NOT_PLAYABLE = 1
        const val TYPE_VIDEO_PLAYABLE = 2
    }
}