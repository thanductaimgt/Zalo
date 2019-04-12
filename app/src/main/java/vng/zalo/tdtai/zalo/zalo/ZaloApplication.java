package vng.zalo.tdtai.zalo.zalo;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;

import androidx.multidex.MultiDexApplication;

public class ZaloApplication extends MultiDexApplication {
    public FirebaseFirestore mFireStore;
    public static DateFormat sDateFormat;
    public static String sCurrentUserPhone;

    @Override
    public void onCreate() {
        super.onCreate();
        mFireStore = FirebaseFirestore.getInstance();
        sDateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        sCurrentUserPhone = "0123456789";
    }
}