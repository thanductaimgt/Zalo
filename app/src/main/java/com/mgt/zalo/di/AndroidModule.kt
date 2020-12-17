package com.mgt.zalo.di

import com.mgt.zalo.base.EmptyActivity
import com.mgt.zalo.service.MessagingService
import com.mgt.zalo.service.UploadService
import com.mgt.zalo.ui.SplashActivity
import com.mgt.zalo.ui.call.CallActivity
import com.mgt.zalo.ui.call.CallModule
import com.mgt.zalo.ui.camera.CameraFragment
import com.mgt.zalo.ui.chat.ChatActivity
import com.mgt.zalo.ui.chat.ChatModule
import com.mgt.zalo.ui.comment.CommentFragment
import com.mgt.zalo.ui.comment.CommentModule
import com.mgt.zalo.ui.create_group.CreateGroupActivity
import com.mgt.zalo.ui.create_group.CreateGroupModule
import com.mgt.zalo.ui.create_post.CreatePostActivity
import com.mgt.zalo.ui.create_post.CreatePostModule
import com.mgt.zalo.ui.edit_media.EditMediaFragment
import com.mgt.zalo.ui.edit_media.EditMediaModule
import com.mgt.zalo.ui.home.HomeActivity
import com.mgt.zalo.ui.home.HomeModule
import com.mgt.zalo.ui.intro.IntroActivity
import com.mgt.zalo.ui.login.LoginActivity
import com.mgt.zalo.ui.media.MediaFragment
import com.mgt.zalo.ui.media.MediaModule
import com.mgt.zalo.ui.post_detail.PostDetailFragment
import com.mgt.zalo.ui.profile.ProfileFragment
import com.mgt.zalo.ui.profile.ProfileModule
import com.mgt.zalo.ui.share.ShareActivity
import com.mgt.zalo.ui.share.ShareModule
import com.mgt.zalo.ui.sign_up.SignUpActivity
import com.mgt.zalo.ui.sign_up.validate_phone.ValidatePhoneFragment
import com.mgt.zalo.ui.story.StoryFragment
import com.mgt.zalo.ui.story.StoryModule
import com.mgt.zalo.ui.story.story_detail.StoryDetailFragment
import com.mgt.zalo.ui.story.story_detail.StoryDetailModule
import dagger.Module
import dagger.android.ContributesAndroidInjector


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

    @ContributesAndroidInjector
    fun emptyActivity(): EmptyActivity

    @ContributesAndroidInjector
    fun signUpActivity(): SignUpActivity

    //service

    @ContributesAndroidInjector
    fun messagingService(): MessagingService

    @ContributesAndroidInjector
    fun uploadService(): UploadService

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

    @ContributesAndroidInjector
    fun postDetailFragment(): PostDetailFragment

    @ContributesAndroidInjector(modules = [CommentModule::class])
    fun commentFragment(): CommentFragment

    @ContributesAndroidInjector(modules = [StoryDetailModule::class])
    fun storyDetailFragment(): StoryDetailFragment

    @ContributesAndroidInjector
    fun validatePhoneFragment(): ValidatePhoneFragment
}