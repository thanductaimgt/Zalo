package com.mgt.zalo.ui.home.watch

import androidx.lifecycle.ViewModel
import com.mgt.zalo.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface WatchModule {
    @Binds
    @IntoMap
    @ViewModelKey(WatchViewModel::class)
    fun bindWatchViewModel(viewModel: WatchViewModel): ViewModel
}