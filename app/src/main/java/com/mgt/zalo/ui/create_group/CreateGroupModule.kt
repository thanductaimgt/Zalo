package com.mgt.zalo.ui.create_group

import androidx.lifecycle.ViewModel
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface CreateGroupModule {
    @Binds
    fun eventListener(activity: CreateGroupActivity):BaseOnEventListener

    @ContributesAndroidInjector
    fun allContactsSubFragment(): AllContactsFragment

    @ContributesAndroidInjector
    fun recentContactsSubFragment():RecentContactsFragment

    @Binds
    @IntoMap
    @ViewModelKey(CreateGroupViewModel::class)
    fun bindCreateGroupViewModel(viewModel: CreateGroupViewModel): ViewModel
}