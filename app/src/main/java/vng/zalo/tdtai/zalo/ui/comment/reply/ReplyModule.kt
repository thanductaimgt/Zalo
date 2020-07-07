package vng.zalo.tdtai.zalo.ui.comment.reply

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.base.BaseOnEventListener
import vng.zalo.tdtai.zalo.data_model.Comment
import vng.zalo.tdtai.zalo.di.ViewModelKey

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