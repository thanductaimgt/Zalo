package com.mgt.zalo.ui.chat

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mgt.zalo.di.ViewModelKey
import com.mgt.zalo.ui.chat.emoji.EmojiFragment
import com.mgt.zalo.ui.chat.emoji.EmojiModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

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