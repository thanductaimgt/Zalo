package vng.zalo.tdtai.zalo.ui.comment.react

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.data_model.react.React
import vng.zalo.tdtai.zalo.di.ViewModelKey

@Module(includes = [ReactModule.ProvideModule::class])
interface ReactModule {
    @Binds
    @IntoMap
    @ViewModelKey(ReactViewModel::class)
    fun bindViewModel(viewModel: ReactViewModel): ViewModel

    @Module
    class ProvideModule {
        @Provides
        fun provideReacts(reactFragment: ReactFragment): HashMap<String, React> {
            return reactFragment.reacts
        }
    }
}