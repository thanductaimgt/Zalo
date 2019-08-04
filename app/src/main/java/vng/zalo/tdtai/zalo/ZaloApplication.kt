package vng.zalo.tdtai.zalo

import androidx.multidex.MultiDexApplication
import vng.zalo.tdtai.zalo.models.UserInfo

class ZaloApplication : MultiDexApplication() {

//    override fun onCreate() {
//        super.onCreate()
//        val applicationComponent = DaggerApplicationComponent
//                .builder()
//                .applicationModule(ApplicationModule(this))
//                .build()
//                ApplicationComponent applicationComponent = DaggerApplicationComponent.create();
//        applicationComponent.inject(this)
//    }

    companion object {
        var currentUser: UserInfo? = null
    }
}