package com.mgt.zalo.ui.comment.react

import androidx.lifecycle.ViewModel
import com.mgt.zalo.data_model.react.React
import com.mgt.zalo.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

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