package vng.zalo.tdtai.zalo.zalo;

import androidx.multidex.MultiDexApplication;

import com.google.firebase.firestore.FirebaseFirestore;

import vng.zalo.tdtai.zalo.zalo.models.UserInfo;

public class ZaloApplication extends MultiDexApplication{
    public static UserInfo currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
//        ApplicationComponent applicationComponent = DaggerApplicationComponent
//                .builder()
//                .applicationModule(new ApplicationModule(this))
//                .build();
//        ApplicationComponent applicationComponent = DaggerApplicationComponent.create();
//        applicationComponent.inject(this);
//        firestore = FirebaseFirestore.getInstance();
    }

    public static FirebaseFirestore getFirebaseInstance(){
        return FirebaseFirestore.getInstance();
    }
}