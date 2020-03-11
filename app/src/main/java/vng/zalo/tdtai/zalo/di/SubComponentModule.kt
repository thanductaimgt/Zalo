package vng.zalo.tdtai.zalo.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import vng.zalo.tdtai.zalo.ui.SplashActivity
import vng.zalo.tdtai.zalo.ui.call.CallActivity
import vng.zalo.tdtai.zalo.ui.call.CallModule
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import vng.zalo.tdtai.zalo.ui.chat.ChatModule
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupActivity
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupModule
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.ui.home.HomeModule
import vng.zalo.tdtai.zalo.ui.login.LoginActivity
import vng.zalo.tdtai.zalo.ui.share.ShareActivity
import vng.zalo.tdtai.zalo.ui.share.ShareModule


@Module
interface SubComponentModule {
    @ContributesAndroidInjector(modules = [HomeModule::class])
    fun homeActivity():HomeActivity

    @ContributesAndroidInjector(modules = [ChatModule::class])
    fun chatActivity():ChatActivity

    @ContributesAndroidInjector(modules = [CallModule::class])
    fun callActivity():CallActivity

    @ContributesAndroidInjector(modules = [CreateGroupModule::class])
    fun createGroupActivity():CreateGroupActivity

    @ContributesAndroidInjector(modules = [ShareModule::class])
    fun shareActivity():ShareActivity

    @ContributesAndroidInjector
    fun loginActivity():LoginActivity

    @ContributesAndroidInjector
    fun splashActivity():SplashActivity
}