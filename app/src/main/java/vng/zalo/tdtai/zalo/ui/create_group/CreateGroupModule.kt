package vng.zalo.tdtai.zalo.ui.create_group

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey

@Module(includes = [CreateGroupModule.ProvideModule::class])
interface CreateGroupModule {
    @Binds
    fun bindClickListener(activity: CreateGroupActivity):View.OnClickListener

    @Binds
    fun bindContext(activity: CreateGroupActivity): Context

    @ContributesAndroidInjector
    fun allContactsSubFragment(): AllContactsSubFragment

    @ContributesAndroidInjector
    fun recentContactsSubFragment():RecentContactsSubFragment

    @Binds
    @IntoMap
    @ViewModelKey(CreateGroupViewModel::class)
    fun bindCreateGroupViewModel(viewModel: CreateGroupViewModel): ViewModel

    @Module
    class ProvideModule{
        @Provides
        fun provideFragmentManager(activity: CreateGroupActivity):FragmentManager{
            return activity.supportFragmentManager
        }
    }
}