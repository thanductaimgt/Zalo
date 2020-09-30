package com.mgt.zalo.ui.story.story_detail

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.data_model.story.StoryGroup
import com.mgt.zalo.di.ViewModelKey

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