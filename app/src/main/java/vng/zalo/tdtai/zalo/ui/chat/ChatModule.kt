package vng.zalo.tdtai.zalo.ui.chat

import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.ui.chat.emoji.EmojiFragment
import vng.zalo.tdtai.zalo.ui.chat.emoji.EmojiModule
import vng.zalo.tdtai.zalo.ui.media.MediaFragment
import vng.zalo.tdtai.zalo.ui.media.MediaModule

@Module(includes = [ChatModule.ProvideModule::class])
interface ChatModule {
    @ContributesAndroidInjector(modules = [EmojiModule::class])
    fun emojiFragment(): EmojiFragment

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    fun bindViewModel(chatViewModel: ChatViewModel): ViewModel

    @Module
    class ProvideModule {
        @Provides
        fun provideIntent(activity: ChatActivity): Intent {
            return activity.intent
        }
    }
}