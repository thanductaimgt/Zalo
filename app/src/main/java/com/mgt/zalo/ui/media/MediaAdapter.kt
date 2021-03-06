package com.mgt.zalo.ui.media

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.media.ImageMedia
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.diff_callback.MediaDiffCallback
import com.mgt.zalo.util.smartLoad
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.item_media_image.view.*
import kotlinx.android.synthetic.main.item_media_video.view.*
import javax.inject.Inject

class MediaAdapter @Inject constructor(
        private val mediaFragment: MediaFragment,
        diffCallback: MediaDiffCallback
) : BaseListAdapter<Media, BaseViewHolder>(diffCallback) {
    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is ImageMedia -> Media.TYPE_IMAGE
            else -> Media.TYPE_VIDEO
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        Log.d(TAG, "detach")
        onMediaNotFocused(holder.itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            Media.TYPE_IMAGE -> ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_image, parent, false).apply {
                photoView.setOnClickListener(mediaFragment)
            })
            else -> VideoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_video, parent, false).apply {
                playerView.videoSurfaceView?.setOnClickListener {
                    mediaFragment.onClick(playerView)
                }
            })
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ImageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val imageMessage = currentList[position] as ImageMedia
            Picasso.get().smartLoad(imageMessage.uri, resourceManager, itemView.photoView) {
                it.fit().centerInside()
            }
        }
    }

    inner class VideoViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val videoMessage = currentList[position] as VideoMedia
            itemView.apply {
                resourceManager.getVideoThumbUri(videoMessage.uri!!) { uri ->
                    Picasso.get().smartLoad(uri, resourceManager, previewImgView) {
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
        itemView.apply {
            if (playerView != null) {
                progressBar.visibility = View.GONE
//            playPauseImgView.visibility = View.GONE
                playbackManager.resume()
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

    private val onLostFocus = {
        mediaFragment.getCurrentItemView()?.let { onMediaNotFocused(it) }
    }

    fun onMediaFocused(itemView: View) {
        itemView.apply {
            if (playerView != null) {
                Log.d(TAG, "focus")
                progressBar.postDelayed({
                    if (playbackManager.isPreparing) {
                        progressBar.visibility = View.VISIBLE
                    }
                }, 1000)

                playbackManager.prepare(
                        (currentList[mediaFragment.viewPager.currentItem] as VideoMedia).uri!!,
                        true,
                        onReady = {
                            previewImgView.visibility = View.GONE
                            onMediaResume(this)
                        }, onLostFocus = onLostFocus)
                playerView.player = playbackManager.exoPlayer
            } else {
                playbackManager.pause()
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