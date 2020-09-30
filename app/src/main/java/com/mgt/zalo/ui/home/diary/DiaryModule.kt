package com.mgt.zalo.ui.home.diary

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.di.ViewModelKey

@Module
interface DiaryModule {
    @Binds
    @IntoMap
    @ViewModelKey(DiaryViewModel::class)
    fun bindDiaryViewModel(viewModel: DiaryViewModel): ViewModel

    @Binds
    fun eventListener(diaryFragment: DiaryFragment):BaseOnEventListener
}