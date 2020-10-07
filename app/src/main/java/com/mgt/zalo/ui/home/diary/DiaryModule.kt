package com.mgt.zalo.ui.home.diary

import androidx.lifecycle.ViewModel
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface DiaryModule {
    @Binds
    @IntoMap
    @ViewModelKey(DiaryViewModel::class)
    fun bindDiaryViewModel(viewModel: DiaryViewModel): ViewModel

    @Binds
    fun eventListener(diaryFragment: DiaryFragment):BaseOnEventListener
}