package vng.zalo.tdtai.zalo.ui.media

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.item_media_image.view.*
import kotlinx.android.synthetic.main.item_media_video.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.manager.PlaybackManager
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.util.MediaDiffCallback
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject

class MediaAdapter @Inject constructor(
        private val mediaFragment: MediaFragment,
        private val resourceManager: ResourceManager,
        private val playbackManager: PlaybackManager,
        diffCallback: MediaDiffCallback
) : BaseListAdapter<Media, BindableViewHolder>(diffCallback) {
    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]){
            is ImageMedia -> Media.TYPE_IMAGE
            else -> Media.TYPE_VIDEO
        }
    }

    override fun onViewDetachedFromWindow(holder: BindableViewHolder) {
        Log.d(TAG, "detach")
        onMediaNotFocused(holder.itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        return when (viewType) {
            Media.TYPE_IMAGE -> ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_image, parent, false).apply {
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
            val imageMessage = currentList[position] as ImageMedia
            Picasso.get().smartLoad(imageMessage.uri, resourceManager, itemView.photoView){
                it.fit().centerInside()
            }
        }
    }

    inner class VideoViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            val videoMessage = currentList[position] as VideoMedia
            itemView.apply {
                resourceManager.getVideoThumbUri(videoMessage.uri!!) { uri ->
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
                playbackManager.play()
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
                playbackManager.pause()
//            if (displayPauseIcon) {
//                playPauseImgView.visibility = View.VISIBLE
//            }
            }
        }
    }

    private val onReady = {
        mediaFragment.getCurrentItemView()?.let {
            onMediaResume(it)
        }
    }
    private val onLostFocus = {
        mediaFragment.getCurrentItemView()?.let { onMediaNotFocused(it) }
    }

    fun onMediaFocused(itemView: View) {
        itemView.playerView?.let {
            Log.d(TAG, "focus")
            itemView.apply {
                progressBar.postDelayed({
                    if(playbackManager.isPreparing){
                        progressBar.visibility = View.VISIBLE
                    }
                },1000)

                playbackManager.prepare((currentList[mediaFragment.viewPager.currentItem] as VideoMedia).uri!!,true, onReady, onLostFocus)
                playerView.player = playbackManager.exoPlayer
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
}