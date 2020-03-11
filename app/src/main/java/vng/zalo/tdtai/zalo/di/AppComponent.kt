package vng.zalo.tdtai.zalo.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import vng.zalo.tdtai.zalo.ZaloApplication
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    SubComponentModule::class
])
interface AppComponent:AndroidInjector<ZaloApplication> {
    @Component.Factory
    interface Factory:AndroidInjector.Factory<ZaloApplication>
}