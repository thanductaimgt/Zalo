package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey

@Module
interface StickerSetModule {
    @Binds
    @IntoMap
    @ViewModelKey(StickerSetViewModel::class)
    fun bindStickerSetViewModel(stickerSetViewModel: StickerSetViewModel): ViewModel
}