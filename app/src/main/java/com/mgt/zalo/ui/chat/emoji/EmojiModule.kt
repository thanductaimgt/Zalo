package com.mgt.zalo.ui.chat.emoji

import androidx.lifecycle.ViewModel
import com.mgt.zalo.di.ViewModelKey
import com.mgt.zalo.ui.chat.emoji.sticker.StickerSetFragment
import com.mgt.zalo.ui.chat.emoji.sticker.StickerSetModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface EmojiModule{
    @ContributesAndroidInjector(modules = [StickerSetModule::class])
    fun stickerSetFragment():StickerSetFragment

    @Binds
    @IntoMap
    @ViewModelKey(EmojiViewModel::class)
    fun bindEmojiViewModel(emojiViewModel: EmojiViewModel): ViewModel
}