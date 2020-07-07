package vng.zalo.tdtai.zalo.ui.comment

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.base.BaseOnEventListener
import vng.zalo.tdtai.zalo.data_model.post.Post
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.ui.comment.react.ReactFragment
import vng.zalo.tdtai.zalo.ui.comment.react.ReactModule
import vng.zalo.tdtai.zalo.ui.comment.reply.ReplyFragment
import vng.zalo.tdtai.zalo.ui.comment.reply.ReplyModule

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