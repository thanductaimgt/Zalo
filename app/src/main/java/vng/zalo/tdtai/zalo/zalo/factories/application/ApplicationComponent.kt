package vng.zalo.tdtai.zalo.zalo.factories.application

import javax.inject.Singleton
import dagger.Component
import vng.zalo.tdtai.zalo.zalo.ZaloApplication

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(zaloApplication: ZaloApplication)
}