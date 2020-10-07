package com.mgt.zalo.ui.home

import com.mgt.zalo.ui.home.chat.ChatFragment
import com.mgt.zalo.ui.home.chat.ChatFragmentModule
import com.mgt.zalo.ui.home.diary.DiaryFragment
import com.mgt.zalo.ui.home.diary.DiaryModule
import com.mgt.zalo.ui.home.test.TestFragment
import com.mgt.zalo.ui.home.watch.WatchFragment
import com.mgt.zalo.ui.home.watch.WatchModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface HomeModule {
    @ContributesAndroidInjector(modules = [DiaryModule::class])
    fun diaryFragment(): DiaryFragment

    @ContributesAndroidInjector(modules = [ChatFragmentModule::class])
    fun chatFragment(): ChatFragment

    @ContributesAndroidInjector(modules = [WatchModule::class])
    fun watchFragment(): WatchFragment

    @ContributesAndroidInjector
    fun testFragment(): TestFragment
}

