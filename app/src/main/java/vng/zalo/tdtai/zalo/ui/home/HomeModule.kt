package vng.zalo.tdtai.zalo.ui.home

import dagger.Module
import dagger.android.ContributesAndroidInjector
import vng.zalo.tdtai.zalo.ui.home.chat.ChatFragment
import vng.zalo.tdtai.zalo.ui.home.chat.ChatFragmentModule
import vng.zalo.tdtai.zalo.ui.home.diary.DiaryFragment
import vng.zalo.tdtai.zalo.ui.home.diary.DiaryModule
import vng.zalo.tdtai.zalo.ui.home.test.TestFragment
import vng.zalo.tdtai.zalo.ui.home.watch.WatchFragment
import vng.zalo.tdtai.zalo.ui.home.watch.WatchModule

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

