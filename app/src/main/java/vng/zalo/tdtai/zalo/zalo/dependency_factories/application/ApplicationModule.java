package vng.zalo.tdtai.zalo.zalo.dependency_factories.application;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private Application application;

    public ApplicationModule(Application application){
        this.application = application;
    }

    @Provides
    FirebaseFirestore providesFirebaseFirestore(){
        return FirebaseFirestore.getInstance();
    }
}