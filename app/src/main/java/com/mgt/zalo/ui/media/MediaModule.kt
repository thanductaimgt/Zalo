package com.mgt.zalo.ui.media

import androidx.lifecycle.ViewModel
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module(includes = [MediaModule.ProvideModule::class])
interface MediaModule {
    @Binds
    @IntoMap
    @ViewModelKey(MediaViewModel::class)
    fun bindViewModel(viewModel: MediaViewModel): ViewModel

    @Module
    class ProvideModule{
        @Provides
        fun provideMedias(mediaFragment: MediaFragment):ArrayList<Media>{
            return mediaFragment.medias
        }
    }
}