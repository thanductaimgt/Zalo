package vng.zalo.tdtai.zalo.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.base.BaseViewModel
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    AndroidModule::class
])
interface AppComponent : AndroidInjector<ZaloApplication> {
    @Component.Factory
    interface Factory : AndroidInjector.Factory<ZaloApplication>

    fun inject(viewModel: BaseViewModel)
}