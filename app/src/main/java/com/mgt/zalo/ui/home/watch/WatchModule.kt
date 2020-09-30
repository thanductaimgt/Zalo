package com.mgt.zalo.ui.home.watch

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import com.mgt.zalo.di.ViewModelKey

@Module
interface WatchModule {
    @Binds
    @IntoMap
    @ViewModelKey(WatchViewModel::class)
    fun bindWatchViewModel(viewModel: WatchViewModel): ViewModel
}