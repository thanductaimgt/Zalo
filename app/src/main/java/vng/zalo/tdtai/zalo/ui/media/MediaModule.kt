package vng.zalo.tdtai.zalo.ui.media

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.di.ViewModelKey

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