package com.mgt.zalo.ui.comment

import androidx.lifecycle.ViewModel
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.data_model.post.Post
import com.mgt.zalo.di.ViewModelKey
import com.mgt.zalo.ui.comment.react.ReactFragment
import com.mgt.zalo.ui.comment.react.ReactModule
import com.mgt.zalo.ui.comment.reply.ReplyFragment
import com.mgt.zalo.ui.comment.reply.ReplyModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [CommentModule.ProvideModule::class])
interface CommentModule {
    @Binds
    @IntoMap
    @ViewModelKey(CommentViewModel::class)
    fun bindViewModel(viewModel: CommentViewModel): ViewModel

    @Binds
    fun eventListener(commentFragment: CommentFragment): BaseOnEventListener

    @ContributesAndroidInjector(modules = [ReactModule::class])
    fun reactFragment(): ReactFragment

    @ContributesAndroidInjector(modules = [ReplyModule::class])
    fun replyFragment(): ReplyFragment

    @Module
    class ProvideModule {
        @Provides
        fun providePost(commentFragment: CommentFragment): Post {
            return commentFragment.post
        }
    }
}