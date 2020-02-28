package vng.zalo.tdtai.zalo.ui.chat.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.ui.chat.ChatViewModel
import vng.zalo.tdtai.zalo.ui.chat.emoji.EmojiComponent

@Module(subcomponents = [EmojiComponent::class])
interface ChatModule{
    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    fun bindChatViewModel(chatViewModel: ChatViewModel): ViewModel
}