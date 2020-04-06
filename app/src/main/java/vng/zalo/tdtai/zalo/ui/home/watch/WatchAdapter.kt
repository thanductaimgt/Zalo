package vng.zalo.tdtai.zalo.ui.home.watch

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_watch.*
import kotlinx.android.synthetic.main.item_watch.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.data_model.post.Watch
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.manager.SharedPrefsManager
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.Utils
import vng.zalo.tdtai.zalo.util.WatchDiffCallback
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject


class WatchAdapter @Inject constructor(
        private val watchFragment: WatchFragment,
        private val resourceManager: ResourceManager,
        private val sharedPrefsManager: SharedPrefsManager,
        private val utils: Utils,
        private val mediaSourceFactory: MediaSourceFactory,
        private val exoPlayer: SimpleExoPlayer,
        diffCallback: WatchDiffCallback
) : BaseListAdapter<Watch, WatchAdapter.WatchViewHolder>(diffCallback) {
    private val objectAnimator = ObjectAnimator().apply {
        setValues(PropertyValuesHolder.ofFloat("rotation", 0f, 360f))
        duration = 6000
        repeatCount = Animation.INFINITE
        interpolator = LinearInterpolator()
    }

    private val playerEventListener = PlayerEventListener()

    init {
        exoPlayer.addListener(playerEventListener)
    }

    var isPreparing = false
    private var isAnimatorStarted = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchViewHolder {
        return WatchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_watch, parent, false)).apply {
            bindOneTime()
        }
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onBindViewHolder(
            holder: WatchViewHolder,
            position: Int,
            payloads: ArrayList<*>
    ) {
        payloads.forEach {
            when (it) {
                Watch.PAYLOAD_FULLSCREEN -> holder.bindFullscreen()
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: WatchViewHolder) {
        Log.d(TAG, "detach")
        onWatchNotFocused(holder.itemView)
    }

    fun onWatchResume(itemView: View) {
        Log.d(TAG, "resume")
        itemView.apply {
            exoPlayer.playWhenReady = true
            playPauseImgView.visibility = View.GONE
            previewImgView.visibility = View.GONE
            musicNameTextView.isSelected = true
            if (isAnimatorStarted) {
                objectAnimator.resume()
            } else {
                objectAnimator.start()
                isAnimatorStarted = true
            }
        }
    }

    fun onWatchPause(itemView: View, displayPauseIcon: Boolean = true) {
        Log.d(TAG, "pause")
        itemView.apply {
            exoPlayer.playWhenReady = false
            if (displayPauseIcon) {
                playPauseImgView.visibility = View.VISIBLE
            }
            musicNameTextView.isSelected = false
            objectAnimator.pause()
        }
    }

    fun onWatchFocused(itemView: View) {
        Log.d(TAG, "focus")
        itemView.apply {
            objectAnimator.target = musicOwnerAvatarImgView
            objectAnimator.start()

            isPreparing = true

            val mediaSource = LoopingMediaSource(
                    mediaSourceFactory.createMediaSource(utils.getUri(
                            currentList[watchFragment.viewPager.currentItem].videoUrl!!
                    ))
            )
            exoPlayer.prepare(mediaSource)
            playerView.player = exoPlayer
        }

        watchFragment.isPaused = false
    }

    fun onWatchNotFocused(itemView: View) {
        Log.d(TAG, "lost focus")
        itemView.apply {
            playerView.player = null

            previewImgView.visibility = View.VISIBLE
            playPauseImgView.visibility = View.GONE
            musicNameTextView.isSelected = false
            musicOwnerAvatarImgView.rotation = 0f
        }
    }

    inner class WatchViewHolder(itemView: View) : BindableViewHolder(itemView) {
        fun bindOneTime() {
            itemView.apply {
                musicIcon.setOnClickListener(watchFragment)
                musicOwnerAvatarImgView.setOnClickListener(watchFragment)
                musicNameTextView.setOnClickListener(watchFragment)
                shareImgView.setOnClickListener(watchFragment)
                commentImgView.setOnClickListener(watchFragment)
                emojiImgView.setOnClickListener(watchFragment)
                watchOwnerAvatarImgView.setOnClickListener(watchFragment)
                nameTextView.setOnClickListener(watchFragment)

                playerView.videoSurfaceView?.setOnClickListener {
                    if (watchFragment.isPaused) {
                        onWatchResume(this)
                    } else {
                        onWatchPause(this)
                    }
                    watchFragment.isPaused = !watchFragment.isPaused
                }
            }
        }

        override fun bind(position: Int) {
            Log.d(TAG, "bind")
            val watch = currentList[position]
            itemView.apply {
//                setViewConstrainRatio(videoMessageLayout, videoMessage.ratio!!)
                Picasso.get().smartLoad(watch.ownerAvatarUrl, resourceManager, watchOwnerAvatarImgView) {
                    it.fit().centerCrop()
                }

//                Picasso.get().smartLoad(watch.musicOwnerAvatarUrl, resourceManager, musicOwnerAvatarImgView) {
//                    it.fit().centerCrop()
//                }

                bindFullscreen()
                bindVideoThumb(watch)

                shareNumTextView.text = utils.getMetricFormat(watch.shareNumCount)
                reactCountTextView.text = utils.getMetricFormat(watch.reactCount)
                commentNumTextView.text = utils.getMetricFormat(watch.commentCount)

                nameTextView.text = String.format("@ %s", watch.ownerName)
//                musicNameTextView.text = watch.musicName

                if (watch.text.isEmpty()) {
                    descTextView.visibility = View.GONE
                } else {
                    descTextView.text = watch.text
                    descTextView.visibility = View.VISIBLE
                }
            }
        }

        fun bindFullscreen() {
            Log.d(TAG, "bindFullscreen")
            itemView.apply {
                if (sharedPrefsManager.isWatchTabFullScreen()) {
                    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    previewImgView.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    previewImgView.scaleType = ImageView.ScaleType.FIT_CENTER
                }
            }
        }

        private fun bindVideoThumb(watch: Watch) {
            itemView.apply {
                resourceManager.getVideoThumbUri(watch.videoUrl!!) { uri ->
                    Picasso.get().smartLoad(uri, resourceManager, previewImgView) {
                        it.fit().centerInside()
                    }
                }

                previewImgView.visibility = View.VISIBLE
            }
        }
    }

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "onPlayerStateChanged")
            if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                watchFragment.getCurrentItemView()?.let {
                    onWatchResume(it)
                }
                isPreparing = false
            }
        }
    }
}