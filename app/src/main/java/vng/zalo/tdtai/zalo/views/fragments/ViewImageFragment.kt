package vng.zalo.tdtai.zalo.views.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_view_image.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.ResourceManager
import vng.zalo.tdtai.zalo.models.message.ImageMessage
import vng.zalo.tdtai.zalo.models.message.ResourceMessage
import vng.zalo.tdtai.zalo.utils.loadCompat

class ViewImageFragment(private val clickListener: View.OnClickListener, val resourceMessage: ResourceMessage, private val exoPlayer: ExoPlayer, private val cacheDataSourceFactory: DataSource.Factory, private val extractorsFactory: ExtractorsFactory) : Fragment() {
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
                Picasso.get().loadCompat(resourceMessage.url)
                        .fit()
                        .centerInside()
                        .into(viewImageImgView)

                viewImageProgressBar.visibility = View.GONE
            }
            else -> {//video
                ResourceManager.getVideoThumbUri(context!!, resourceMessage.url) {
                    Picasso.get().loadCompat(it)
                            .fit()
                            .centerInside()
                            .into(viewImageImgView)
                }

                viewImagePlayerView.controllerAutoShow = false

                mediaSource = LoopingMediaSource(
                        ProgressiveMediaSource.Factory(
                                cacheDataSourceFactory,
                                extractorsFactory
                        ).createMediaSource(Uri.parse(resourceMessage.url))
                )

                var isPreparing = true
                viewImageProgressBar.visibility = View.VISIBLE

                exoPlayer.addListener(object : Player.EventListener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
                            viewImageImgView.visibility = View.GONE
                            viewImageProgressBar.visibility = View.GONE
                            isPreparing = false
                        }
                    }
                })

                exoPlayer.prepare(mediaSource)
                exoPlayer.playWhenReady = true
                viewImagePlayerView.player = exoPlayer

                viewImagePlayerView.setOnClickListener(clickListener)
            }
        }

        viewImageImgView.setOnClickListener(clickListener)
    }
}