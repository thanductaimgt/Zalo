package vng.zalo.tdtai.zalo.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import vng.zalo.tdtai.zalo.service.MessagingService
import vng.zalo.tdtai.zalo.ui.SplashActivity
import vng.zalo.tdtai.zalo.ui.call.CallActivity
import vng.zalo.tdtai.zalo.ui.call.CallModule
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import vng.zalo.tdtai.zalo.ui.chat.ChatModule
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupActivity
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupModule
import vng.zalo.tdtai.zalo.ui.create_post.CreatePostActivity
import vng.zalo.tdtai.zalo.ui.create_post.CreatePostModule
import vng.zalo.tdtai.zalo.ui.edit_media.EditMediaFragment
import vng.zalo.tdtai.zalo.ui.edit_media.EditMediaModule
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.ui.home.HomeModule
import vng.zalo.tdtai.zalo.ui.camera.CameraFragment
import vng.zalo.tdtai.zalo.ui.intro.IntroActivity
import vng.zalo.tdtai.zalo.ui.login.LoginActivity
import vng.zalo.tdtai.zalo.ui.media.MediaFragment
import vng.zalo.tdtai.zalo.ui.media.MediaModule
import vng.zalo.tdtai.zalo.ui.profile.ProfileFragment
import vng.zalo.tdtai.zalo.ui.profile.ProfileModule
import vng.zalo.tdtai.zalo.ui.share.ShareActivity
import vng.zalo.tdtai.zalo.ui.share.ShareModule
import vng.zalo.tdtai.zalo.ui.story.StoryFragment
import vng.zalo.tdtai.zalo.ui.story.StoryModule


@Module
interface AndroidModule {
    //activity

    @ContributesAndroidInjector(modules = [HomeModule::class])
    fun homeActivity(): HomeActivity

    @ContributesAndroidInjector(modules = [ChatModule::class])
    fun chatActivity(): ChatActivity

    @ContributesAndroidInjector(modules = [CallModule::class])
    fun callActivity(): CallActivity

    @ContributesAndroidInjector(modules = [CreateGroupModule::class])
    fun createGroupActivity(): CreateGroupActivity

    @ContributesAndroidInjector(modules = [ShareModule::class])
    fun shareActivity(): ShareActivity

    @ContributesAndroidInjector(modules = [CreatePostModule::class])
    fun createPostActivity(): CreatePostActivity

    @ContributesAndroidInjector
    fun loginActivity(): LoginActivity

    @ContributesAndroidInjector
    fun introActivity(): IntroActivity

    @ContributesAndroidInjector
    fun splashActivity(): SplashActivity

    //service

    @ContributesAndroidInjector
    fun messagingService(): MessagingService

    //fragment

    @ContributesAndroidInjector(modules = [StoryModule::class])
    fun storyFragment(): StoryFragment

    @ContributesAndroidInjector(modules = [ProfileModule::class])
    fun profileFragment(): ProfileFragment

    @ContributesAndroidInjector
    fun cameraFragment(): CameraFragment

    @ContributesAndroidInjector(modules = [MediaModule::class])
    fun mediaFragment(): MediaFragment

    @ContributesAndroidInjector(modules = [EditMediaModule::class])
    fun createStoryFragment(): EditMediaFragment
}