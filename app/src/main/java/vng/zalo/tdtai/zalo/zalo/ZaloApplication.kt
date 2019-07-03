package vng.zalo.tdtai.zalo.zalo

import androidx.multidex.MultiDexApplication
import com.google.firebase.firestore.FirebaseFirestore
import vng.zalo.tdtai.zalo.zalo.models.UserInfo

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

        val firebaseInstance: FirebaseFirestore
            get() = FirebaseFirestore.getInstance()
    }
}