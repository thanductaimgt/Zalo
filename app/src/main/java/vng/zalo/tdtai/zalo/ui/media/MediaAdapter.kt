package vng.zalo.tdtai.zalo.ui.media

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.item_media_image.view.*
import kotlinx.android.synthetic.main.item_media_video.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.data_model.message.ImageMessage
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.data_model.message.VideoMessage
import vng.zalo.tdtai.zalo.util.MessageDiffCallback
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.Utils
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject

class MediaAdapter @Inject constructor(
        private val mediaFragment: MediaFragment,
        private val resourceManager: ResourceManager,
        private val utils: Utils,
        val exoPlayer: SimpleExoPlayer,
        private val mediaSourceFactory: MediaSourceFactory,
        diffCallback: MessageDiffCallback
) : BaseListAdapter<Message, BindableViewHolder>(diffCallback) {
    var isPreparing = false
    private val playerEventListener = PlayerEventListener()

    init {
        exoPlayer.addListener(playerEventListener)
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].type!!
    }

    override fun onViewDetachedFromWindow(holder: BindableViewHolder) {
        Log.d(TAG, "detach")
        onMediaNotFocused(holder.itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        return when (viewType) {
            Message.TYPE_IMAGE -> ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_image, parent, false).apply {
                photoView.setOnClickListener(mediaFragment)
            })
            else -> VideoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_video, parent, false).apply {
                playerView.videoSurfaceView?.setOnClickListener{
                    mediaFragment.onClick(playerView)
                }
            })
        }
    }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ImageViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            val imageMessage = currentList[position] as ImageMessage
            Picasso.get().smartLoad(imageMessage.url, resourceManager, itemView.photoView){
                it.fit().centerInside()
            }
        }
    }

    inner class VideoViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            val videoMessage = currentList[position] as VideoMessage
            itemView.apply {
                resourceManager.getVideoThumbUri(videoMessage.url) { uri ->
                    Picasso.get().smartLoad(uri, resourceManager, previewImgView){
                        it.fit().centerInside()
                    }
                }

                previewImgView.visibility = View.VISIBLE

                playerView.controllerAutoShow = false
            }
        }
    }

    fun onMediaResume(itemView: View) {
        Log.d(TAG, "resume")
        itemView.playerView?.let{
            itemView.apply {
                exoPlayer.playWhenReady = true
                progressBar.visibility = View.GONE
//            playPauseImgView.visibility = View.GONE
                previewImgView.visibility = View.GONE
            }
        }
    }

    fun onMediaPause(itemView: View, displayPauseIcon: Boolean = true) {
        itemView.playerView?.let {
            Log.d(TAG, "pause")
            itemView.apply {
                exoPlayer.playWhenReady = false
//            if (displayPauseIcon) {
//                playPauseImgView.visibility = View.VISIBLE
//            }
            }
        }
    }

    fun onMediaFocused(itemView: View) {
        itemView.playerView?.let {
            Log.d(TAG, "focus")
            itemView.apply {
                progressBar.postDelayed({
                    if(isPreparing){
                        progressBar.visibility = View.VISIBLE
                    }
                },1000)

                isPreparing = true
//                exoPlayer.addListener(playerEventListener)

                val mediaSource = LoopingMediaSource(
                        mediaSourceFactory.createMediaSource(utils.getUri(
                                (currentList[mediaFragment.viewPager.currentItem] as VideoMessage).url
                        ))
                )
                exoPlayer.prepare(mediaSource)
                playerView.player = exoPlayer
            }
        }
    }

    private fun onMediaNotFocused(itemView: View) {
        Log.d(TAG, "lost focus")
        itemView.playerView?.let {
            itemView.apply {
                playerView.player = null

                previewImgView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
//            playPauseImgView.visibility = View.GONE
            }
        }
    }

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "onPlayerStateChanged")
            if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                mediaFragment.getCurrentItemView()?.let {
                    onMediaResume(it)
                }
                isPreparing = false
            }
        }
    }
}