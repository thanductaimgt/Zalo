package com.mgt.zalo.ui.create_post

import androidx.lifecycle.ViewModel
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface CreatePostModule {
    @Binds
    @IntoMap
    @ViewModelKey(CreatePostViewModel::class)
    fun viewModel(viewModel: CreatePostViewModel): ViewModel

    @Binds
    fun eventListener(createPostActivity: CreatePostActivity): BaseOnEventListener
}