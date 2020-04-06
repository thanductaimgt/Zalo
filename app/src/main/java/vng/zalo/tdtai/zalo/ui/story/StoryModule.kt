package vng.zalo.tdtai.zalo.ui.story

import android.view.View
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup

@Module(includes = [StoryModule.ProvideModule::class])
interface StoryModule {
    @Binds
    @IntoMap
    @ViewModelKey(StoryViewModel::class)
    fun bindViewModel(viewModel: StoryViewModel): ViewModel

    @Binds
    fun clickListener(storyFragment: StoryFragment): View.OnClickListener

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