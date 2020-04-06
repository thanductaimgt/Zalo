package vng.zalo.tdtai.zalo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.Multibinds
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.manager.CallService
import vng.zalo.tdtai.zalo.manager.SipCallService
import vng.zalo.tdtai.zalo.service.AlwaysRunningNotificationService
import vng.zalo.tdtai.zalo.service.NotificationService
import vng.zalo.tdtai.zalo.ui.chat.CacheDataSourceFactory
import javax.inject.Singleton

@Module(includes = [AppModule.ProvideModule::class])
interface AppModule{
    @Binds
    @Singleton
    fun bindCallService(sipCallService: SipCallService):CallService

    @Binds
    @Singleton
    fun bindNotificationService(alwaysRunningNotificationService: AlwaysRunningNotificationService):NotificationService

    @Binds
    fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Multibinds
    fun bindViewModelMap():Map<Class<out ViewModel>, ViewModel>

    @Module
    class ProvideModule{
        @Provides
        @Singleton
        fun provideExoPlayer(application:ZaloApplication): SimpleExoPlayer {
            return SimpleExoPlayer.Builder(application)
                    .setBandwidthMeter(DefaultBandwidthMeter.Builder(application).build())
                    .setTrackSelector(DefaultTrackSelector(application))
                    .build()
                    .apply { playWhenReady = false }
        }

        @Provides
        @Singleton
        fun provideMediaSourceFactory(application: ZaloApplication):MediaSourceFactory{
            return ProgressiveMediaSource.Factory(
                    CacheDataSourceFactory(application, 100 * 1024 * 1024, 10 * 1024 * 1024),
                    DefaultExtractorsFactory()
            )
        }
    }
}