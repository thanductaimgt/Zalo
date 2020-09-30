package com.mgt.zalo.ui.profile

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.di.ViewModelKey
import com.mgt.zalo.ui.profile.diary.ProfileDiaryFragment
import com.mgt.zalo.ui.profile.media.ProfileMediaFragment

@Module(includes = [ProfileModule.ProvideModule::class])
interface ProfileModule {
    @ContributesAndroidInjector
    fun profileDiaryFragment(): ProfileDiaryFragment

    @ContributesAndroidInjector
    fun profileMediaFragment(): ProfileMediaFragment

    @Binds
    fun eventListener(profileFragment: ProfileFragment): BaseOnEventListener

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    fun bindViewModel(viewModel: ProfileViewModel): ViewModel

    @Module
    class ProvideModule{
        @Provides
        fun provideUserId(profileFragment: ProfileFragment):String{
            return profileFragment.userId
        }
    }
}