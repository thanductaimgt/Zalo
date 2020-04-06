package vng.zalo.tdtai.zalo.ui.chat.emoji

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.ui.chat.emoji.sticker.StickerSetFragment
import vng.zalo.tdtai.zalo.ui.chat.emoji.sticker.StickerSetModule

@Module
interface EmojiModule{
    @ContributesAndroidInjector(modules = [StickerSetModule::class])
    fun stickerSetFragment():StickerSetFragment

    @Binds
    @IntoMap
    @ViewModelKey(EmojiViewModel::class)
    fun bindEmojiViewModel(emojiViewModel: EmojiViewModel): ViewModel
}