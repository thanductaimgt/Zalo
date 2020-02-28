package vng.zalo.tdtai.zalo.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import vng.zalo.tdtai.zalo.ui.call.di.CallComponent
import vng.zalo.tdtai.zalo.ui.chat.di.ChatComponent
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupActivity
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupComponent
import vng.zalo.tdtai.zalo.ui.home.contacts.ContactSubFragmentComponent
import vng.zalo.tdtai.zalo.ui.home.contacts.OfficialAccountComponent

@Module(subcomponents = [
    ChatComponent::class,
    CallComponent::class,
    CreateGroupComponent::class,
    ContactSubFragmentComponent::class,
    OfficialAccountComponent::class
])
interface SubComponentModule {
    //contibuteandroidinjectors goes here
    @ContributesAndroidInjector
    fun createGroupActivity(): CreateGroupActivity
}