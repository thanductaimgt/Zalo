package vng.zalo.tdtai.zalo.common

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_media_video_preview.view.*
import kotlinx.android.synthetic.main.part_media_preview.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.manager.PlaybackManager
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.util.MediaDiffCallback
import vng.zalo.tdtai.zalo.util.Utils
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject

class MediaPreviewAdapter @Inject constructor(
        private val clickListener: View.OnClickListener,
        private val resourceManager: ResourceManager,
        private val utils: Utils,
        private val playbackManager: PlaybackManager,
        diffCallback: MediaDiffCallback
) : BaseListAdapter<Media, MediaPreviewAdapter.MediaPreviewHolder>(diffCallback) {
    var moreCount = 0
    var showPlayIcon = true

    private val onReady = {
        playbackManager.play()
    }

    private val onEnded = {

    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is ImageMedia -> Media.TYPE_IMAGE
            else -> Media.TYPE_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaPreviewHolder {
        return when (viewType) {
            Media.TYPE_IMAGE -> ImageMediaPreviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_image_preview, parent, false))
            else -> VideoMediaPreviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_video_preview, parent, false))
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

    abstract inner class MediaPreviewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            bindRatio()
            itemView.imageView.visibility = View.VISIBLE
            bindDisplayMore(position)
        }

        fun bindRatio() {
            itemView.apply {
                imageView.post {
                    imageView.layoutParams = imageView.layoutParams.apply {
                        if (itemCount == 1) {
                            val media = currentList[0]

                            this as ConstraintLayout.LayoutParams
                            dimensionRatio = utils.getAdjustedRatio(media.ratio!!)
//                            val size = utils.getSize(imageMedia.ratio!!)
//                            val floatRatio = size.width / size.height.toFloat()
                            height = 0//(imageView.width / floatRatio).toInt()
                        } else {
                            height = ConstraintLayout.LayoutParams.MATCH_PARENT
                        }
                    }
                }
            }
        }

        fun bindDisplayMore(position: Int) {
            itemView.apply {
                displayMoreTextView.visibility = if (moreCount > 0 && position == currentList.lastIndex) {
                    displayMoreTextView.text = "+$moreCount"
                    imageView.foreground = ColorDrawable(ContextCompat.getColor(context, R.color.blackTransparent))
//                    videoDurationTV.visibility = View.GONE
                    View.VISIBLE
                } else {
                    imageView.foreground = null
//                    videoDurationTV.visibility = if (currentList[position] is VideoMedia) View.VISIBLE else View.GONE
                    View.GONE
                }
            }
        }

        open fun bindOneTime() {
            itemView.setOnClickListener(clickListener)
        }
    }

    inner class VideoMediaPreviewHolder(itemView: View) : MediaPreviewHolder(itemView) {
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
                if (showPlayIcon) {
                    playImgView.visibility = View.VISIBLE
                } else {
                    playImgView.visibility = View.GONE
                }
            }
        }

        fun playVideo(videoMedia: VideoMedia) {
            itemView.apply {
                playImgView.visibility = View.GONE
                playbackManager.prepare(videoMedia.uri!!, false, onReady = {
                    imageView.visibility = View.INVISIBLE
                    playbackManager.play()
                }, onLostFocus = {
                    if (showPlayIcon) {
                        playImgView.visibility = View.VISIBLE
                    }
                    playbackManager.pause()
                    playerView.player = null
                    imageView.apply { visibility = View.VISIBLE }
                })
                playerView.player = playbackManager.exoPlayer
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

            val imageMedia = currentList[position] as ImageMedia
            itemView.apply {
                Picasso.get().smartLoad(imageMedia.uri, resourceManager, imageView) {
                    it.fit().centerCrop()
                }
            }
        }
    }

    private var curPosition = 0

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
    }
}