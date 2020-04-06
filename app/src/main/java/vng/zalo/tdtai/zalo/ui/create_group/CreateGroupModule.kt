package vng.zalo.tdtai.zalo.ui.create_group

import android.view.View
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey

@Module
interface CreateGroupModule {
    @Binds
    fun bindClickListener(activity: CreateGroupActivity):View.OnClickListener

    @ContributesAndroidInjector
    fun allContactsSubFragment(): AllContactsFragment

    @ContributesAndroidInjector
    fun recentContactsSubFragment():RecentContactsFragment

    @Binds
    @IntoMap
    @ViewModelKey(CreateGroupViewModel::class)
    fun bindCreateGroupViewModel(viewModel: CreateGroupViewModel): ViewModel
}