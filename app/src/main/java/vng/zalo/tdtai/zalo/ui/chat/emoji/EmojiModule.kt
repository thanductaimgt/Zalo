package vng.zalo.tdtai.zalo.ui.chat.emoji

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.ui.chat.emoji.sticker.StickerSetComponent

@Module(subcomponents = [StickerSetComponent::class])
interface EmojiModule{
    @Binds
    @IntoMap
    @ViewModelKey(EmojiViewModel::class)
    fun bindEmojiViewModel(emojiViewModel: EmojiViewModel): ViewModel
}