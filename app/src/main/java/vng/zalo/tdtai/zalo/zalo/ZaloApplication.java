package vng.zalo.tdtai.zalo.zalo;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;

import androidx.multidex.MultiDexApplication;

public class ZaloApplication extends MultiDexApplication {
    public FirebaseFirestore mFireStore;
    public DateFormat mDateFormat;

    @Override
    public void onCreate() {
        super.onCreate();
        mFireStore = FirebaseFirestore.getInstance();
        mDateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
    }
}