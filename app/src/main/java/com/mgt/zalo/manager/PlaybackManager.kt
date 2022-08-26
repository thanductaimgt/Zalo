package com.mgt.zalo.manager

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.ui.chat.CacheDataSourceFactory
import com.mgt.zalo.util.Utils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
        application: ZaloApplication,
        private val utils: Utils
) {
    private val mediaSourceFactory: MediaSource.Factory = createMediaSourceFactory(application)
    var isPreparing = false
    var lastPlaybackState: Int? = null
    private val playerEventListener = object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//            if (isPreparing && playbackState == ExoPlayer.STATE_READY) {
//                onReady?.invoke()
//                isPreparing = false
//            }else if(!isPreparing && playbackState == ExoPlayer.STATE_ENDED){
//                onEnded?.invoke()
//            }
            if (playbackState != lastPlaybackState) {
                when (playbackState) {
                    ExoPlayer.STATE_READY -> {
                        onReady?.invoke()
                        isPreparing = false
                    }
                    ExoPlayer.STATE_ENDED -> onEnded?.invoke()
                }
                lastPlaybackState = playbackState
            }
        }
    }

    val exoPlayer: SimpleExoPlayer = createPlayer(application).apply { addListener(playerEventListener) }

    private var onReady: (() -> Any?)? = null
    private var onLostFocus: (() -> Any?)? = null
    private var onEnded: (() -> Any?)? = null
    private var currentVolume: Float = 0f

    private fun createPlayer(application: ZaloApplication): SimpleExoPlayer {
        return SimpleExoPlayer.Builder(application)
                .setBandwidthMeter(DefaultBandwidthMeter.Builder(application).build())
                .setTrackSelector(DefaultTrackSelector(application))
                .build()
                .apply { playWhenReady = false }
    }

    private fun createMediaSourceFactory(application: ZaloApplication): MediaSourceFactory {
        return ProgressiveMediaSource.Factory(
                CacheDataSourceFactory(application, 100 * 1024 * 1024, 10 * 1024 * 1024),
                DefaultExtractorsFactory()
        )
    }

    fun prepare(uri: String, doLooping: Boolean, onReady: () -> Any?, onLostFocus: (() -> Any?)? = null, onEnded: (() -> Any?)? = null) {
        pause()

        try {
            this.onLostFocus?.invoke()
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        try {
            var mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(uri))
            if (doLooping) {
                mediaSource = LoopingMediaSource(mediaSource)
            }

            this.onReady = onReady
            this.onLostFocus = onLostFocus
            this.onEnded = onEnded

            isPreparing = true
            exoPlayer.prepare(mediaSource)
        } catch (t: Throwable) {
            t.printStackTrace()
            isPreparing = false
        }
    }

    fun resume() {
        exoPlayer.playWhenReady = true
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun mute() {
        if (exoPlayer.volume > 0) {
            currentVolume = exoPlayer.volume
            exoPlayer.volume = 0f
        }
    }

    fun unMute() {
        if (exoPlayer.volume == 0f) {
            exoPlayer.volume = currentVolume
        }
    }

    fun isMuted(): Boolean {
        return exoPlayer.volume == 0f
    }
}