package vng.zalo.tdtai.zalo.ui.chat.view_image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_view_image.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.message.ImageMessage
import vng.zalo.tdtai.zalo.model.message.ResourceMessage
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.utils.loadCompat
import javax.inject.Inject

class ViewImageFragment(
        private val clickListener: View.OnClickListener,
        val resourceMessage: ResourceMessage,
        private val exoPlayer: ExoPlayer,
        private val cacheDataSourceFactory: DataSource.Factory,
        private val extractorsFactory: ExtractorsFactory
) : DaggerFragment() {
    @Inject lateinit var resourceManager: ResourceManager
    @Inject lateinit var utils: Utils

    private lateinit var mediaSource: MediaSource

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() {
        when (resourceMessage) {
            is ImageMessage -> {
                Picasso.get().loadCompat(resourceMessage.url, resourceManager)
                        .fit()
                        .centerInside()
                        .into(viewImagePhotoView)

                viewImageProgressBar.visibility = View.GONE
            }
            else -> {//video
                resourceManager.getVideoThumbUri(requireContext(), resourceMessage.url) {
                    Picasso.get().loadCompat(it, resourceManager)
                            .fit()
                            .centerInside()
                            .into(viewImagePhotoView)
                }

                viewImagePlayerView.controllerAutoShow = false

                mediaSource = LoopingMediaSource(
                        ProgressiveMediaSource.Factory(
                                cacheDataSourceFactory,
                                extractorsFactory
                        ).createMediaSource(utils.getUri(resourceMessage.url))
                )

                var isPreparing = true
                viewImageProgressBar.visibility = View.VISIBLE

                exoPlayer.addListener(object : Player.EventListener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                            viewImagePhotoView.visibility = View.GONE
                            viewImageProgressBar.visibility = View.GONE
                            isPreparing = false
                        }
                    }
                })

                exoPlayer.prepare(mediaSource)
                viewImagePlayerView.player = exoPlayer

                viewImagePlayerView.setOnClickListener(clickListener)
            }
        }

        viewImagePhotoView.setOnClickListener(clickListener)
    }
}