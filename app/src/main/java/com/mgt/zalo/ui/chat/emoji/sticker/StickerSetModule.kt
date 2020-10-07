package com.mgt.zalo.ui.chat.emoji.sticker

import androidx.lifecycle.ViewModel
import com.mgt.zalo.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module(includes = [StickerSetModule.ProvideModule::class])
interface StickerSetModule {
    @Binds
    @IntoMap
    @ViewModelKey(StickerSetViewModel::class)
    fun bindStickerSetViewModel(viewModel: StickerSetViewModel): ViewModel

    @Module
    class ProvideModule{
        @Provides
        fun bucketName(stickerSetFragment: StickerSetFragment):String{
            return stickerSetFragment.bucketName
        }
    }
}