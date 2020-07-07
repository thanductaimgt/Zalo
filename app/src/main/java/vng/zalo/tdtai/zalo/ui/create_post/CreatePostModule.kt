package vng.zalo.tdtai.zalo.ui.create_post

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.base.BaseOnEventListener
import vng.zalo.tdtai.zalo.di.ViewModelKey

@Module
interface CreatePostModule {
    @Binds
    @IntoMap
    @ViewModelKey(CreatePostViewModel::class)
    fun viewModel(viewModel: CreatePostViewModel): ViewModel

    @Binds
    fun eventListener(createPostActivity: CreatePostActivity): BaseOnEventListener
}