package vng.zalo.tdtai.zalo.zalo

import androidx.multidex.MultiDexApplication
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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

        val firebaseFirestore: FirebaseFirestore
            get() = FirebaseFirestore.getInstance()
        val firebaseStorage: FirebaseStorage
            get() = FirebaseStorage.getInstance()
    }
}