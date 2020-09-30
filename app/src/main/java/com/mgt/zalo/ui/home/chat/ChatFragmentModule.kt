package com.mgt.zalo.ui.home.chat

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.di.ViewModelKey

@Module
interface ChatFragmentModule {
    @Binds
    fun eventListener(chatFragment: ChatFragment): BaseOnEventListener

    @Binds
    @IntoMap
    @ViewModelKey(ChatFragmentViewModel::class)
    fun bindRoomItemsViewModel(viewModel: ChatFragmentViewModel): ViewModel
}