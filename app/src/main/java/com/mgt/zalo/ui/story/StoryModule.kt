package com.mgt.zalo.ui.story

import androidx.lifecycle.ViewModel
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.data_model.story.StoryGroup
import com.mgt.zalo.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module(includes = [StoryModule.ProvideModule::class])
interface StoryModule {
    @Binds
    @IntoMap
    @ViewModelKey(StoryViewModel::class)
    fun bindViewModel(viewModel: StoryViewModel): ViewModel

    @Binds
    fun eventListener(storyFragment: StoryFragment): BaseOnEventListener

    @Module
    class ProvideModule{
        @Provides
        fun storyGroup(storyFragment: StoryFragment):StoryGroup{
            return storyFragment.curStoryGroup
        }

        @Provides
        fun storyGroups(storyFragment: StoryFragment):List<StoryGroup>{
            return storyFragment.storyGroups
        }
    }
}