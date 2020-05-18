package vng.zalo.tdtai.zalo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.Multibinds
import vng.zalo.tdtai.zalo.manager.CallService
import vng.zalo.tdtai.zalo.manager.SipCallService
import vng.zalo.tdtai.zalo.service.AlwaysRunningNotificationService
import vng.zalo.tdtai.zalo.service.NotificationService
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