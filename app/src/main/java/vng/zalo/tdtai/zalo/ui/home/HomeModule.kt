package vng.zalo.tdtai.zalo.ui.home

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.ui.home.chat.ChatFragment
import vng.zalo.tdtai.zalo.ui.home.chat.ChatFragmentModule
import vng.zalo.tdtai.zalo.ui.home.contacts.ContactFragment
import vng.zalo.tdtai.zalo.ui.home.contacts.ContactModule
import vng.zalo.tdtai.zalo.ui.home.diary.DiaryFragment
import vng.zalo.tdtai.zalo.ui.home.group.GroupFragment
import vng.zalo.tdtai.zalo.ui.home.group.GroupFragmentModule
import vng.zalo.tdtai.zalo.ui.home.more.MoreFragment
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Named

@Module(includes = [HomeModule.ProvideModule::class])
interface HomeModule {
    @ContributesAndroidInjector(modules = [ChatFragmentModule::class])
    fun chatFragment(): ChatFragment

    @ContributesAndroidInjector(modules = [ContactModule::class])
    fun contactFragment(): ContactFragment

    @ContributesAndroidInjector(modules = [GroupFragmentModule::class])
    fun groupFragment(): GroupFragment

    @ContributesAndroidInjector
    fun diaryFragment(): DiaryFragment

    @ContributesAndroidInjector
    fun moreFragment(): MoreFragment

    @Binds
    fun bindContext(homeActivity: HomeActivity): Context

    @Binds
    @IntoMap
    @ViewModelKey(RoomItemsViewModel::class)
    fun bindRoomItemsViewModel(viewModel: RoomItemsViewModel): ViewModel

    @Module
    class ProvideModule {
        @Provides
        @Named(Constants.ACTIVITY_NAME)
        fun provideFragmentManager(homeActivity: HomeActivity): FragmentManager {
            return homeActivity.supportFragmentManager
        }
    }
}