package vng.zalo.tdtai.zalo.ui.chat

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.android.synthetic.main.activity_room.*
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.ui.chat.emoji.EmojiFragment
import vng.zalo.tdtai.zalo.ui.chat.emoji.EmojiModule
import vng.zalo.tdtai.zalo.ui.chat.view_image.ViewImageFragment
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Named

@Module(includes = [ChatModule.ProvideModule::class])
interface ChatModule {
    @ContributesAndroidInjector(modules = [EmojiModule::class])
    fun emojiFragment():EmojiFragment

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    fun bindChatViewModel(chatViewModel: ChatViewModel): ViewModel

    @Binds
    @Named(Constants.ACTIVITY_NAME)
    fun bindClickListener(activity: ChatActivity): View.OnClickListener

    @Binds
    fun bindLongClickListener(activity: ChatActivity): View.OnLongClickListener

    @Binds
    fun bindContext(activity: ChatActivity): Context

    @ContributesAndroidInjector
    fun viewImageFragment():ViewImageFragment

    @Module
    class ProvideModule{
        @Provides
        fun provideIntent(activity: ChatActivity): Intent {
            return activity.intent
        }

        @Provides
        @Named(Constants.ACTIVITY_NAME)
        fun provideFragmentManager(activity: ChatActivity): FragmentManager {
            return activity.supportFragmentManager
        }

        @Provides
        fun provideExoPlayer(context: Context): ExoPlayer {
            return SimpleExoPlayer.Builder(context)
                    .setBandwidthMeter(DefaultBandwidthMeter.Builder(context).build())
                    .setTrackSelector(DefaultTrackSelector(context))
                    .build()
        }

        @Provides
        fun provideDataSourceFactory(context:Context):DataSource.Factory{
            return CacheDataSourceFactory(context, 100 * 1024 * 1024, 10 * 1024 * 1024)
        }

        @Provides
        fun provideExtractorsFactory(context:Context): ExtractorsFactory {
            return DefaultExtractorsFactory()
        }
    }
}