package vng.zalo.tdtai.zalo.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.ui.call.di.CallComponent
import vng.zalo.tdtai.zalo.ui.chat.di.ChatComponent
import vng.zalo.tdtai.zalo.ui.home.contacts.ContactSubFragmentComponent
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    SubComponentModule::class
])
interface AppComponent:AndroidInjector<ZaloApplication> {
    fun chatComponentFactory():ChatComponent.Factory
    fun callComponentFactory():CallComponent.Factory

    @Component.Factory
    interface Factory{
        fun create(@BindsInstance application: ZaloApplication): AppComponent
    }
}