package vng.zalo.tdtai.zalo.ui.story.story_detail

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.base.BaseOnEventListener
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.di.ViewModelKey

@Module(includes = [StoryDetailModule.ProvideModule::class])
interface StoryDetailModule {
    @Binds
    @IntoMap
    @ViewModelKey(StoryDetailViewModel::class)
    fun bindViewModel(viewModel: StoryDetailViewModel): ViewModel

    @Binds
    fun eventListener(storyDetailFragment: StoryDetailFragment): BaseOnEventListener

    @Module
    class ProvideModule{
        @Provides
        fun storyGroup(storyDetailFragment: StoryDetailFragment):StoryGroup{
            return storyDetailFragment.storyGroup
        }
    }
}