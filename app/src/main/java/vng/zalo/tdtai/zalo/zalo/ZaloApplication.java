package vng.zalo.tdtai.zalo.zalo;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;

import javax.inject.Inject;

import androidx.multidex.MultiDexApplication;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.application.ApplicationComponent;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.application.ApplicationModule;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.application.DaggerApplicationComponent;

public class ZaloApplication extends MultiDexApplication{
    public static DateFormat dateFormat;
    public static String currentUserPhone;

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
        dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        currentUserPhone = "0123456789";
    }

    public static FirebaseFirestore getFirebaseInstance(){
        return FirebaseFirestore.getInstance();
    }
}