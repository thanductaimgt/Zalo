package com.mgt.zalo.ui.comment.reply

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.data_model.Comment
import com.mgt.zalo.di.ViewModelKey

@Module(includes = [ReplyModule.ProvideModule::class])
interface ReplyModule {
    @Binds
    @IntoMap
    @ViewModelKey(ReplyViewModel::class)
    fun bindViewModel(viewModel: ReplyViewModel): ViewModel

    @Binds
    fun eventListener(replyFragment: ReplyFragment): BaseOnEventListener

    @Module
    class ProvideModule {
        @Provides
        fun provideComment(replyFragment: ReplyFragment): Comment {
            return replyFragment.comment
        }
    }
}