package com.mgt.zalo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.Multibinds
import com.mgt.zalo.manager.CallService
import com.mgt.zalo.manager.SipCallService
import com.mgt.zalo.service.AlwaysRunningNotificationService
import com.mgt.zalo.service.NotificationService
import javax.inject.Singleton

@Module
interface AppModule{
    @Binds
    @Singleton
    fun bindCallService(sipCallService: SipCallService):CallService

    @Binds
    @Singleton
    fun bindNotificationService(alwaysRunningNotificationService: AlwaysRunningNotificationService):NotificationService

    @Binds
    fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Multibinds
    fun bindViewModelMap():Map<Class<out ViewModel>, ViewModel>
}