package com.mgt.zalo.di

import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.base.BaseViewModel
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
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
    fun inject(adapter: BaseListAdapter<Any, BaseViewHolder>)
}