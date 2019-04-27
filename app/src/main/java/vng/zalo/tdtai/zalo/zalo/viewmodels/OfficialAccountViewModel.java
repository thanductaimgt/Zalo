package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_OFFICIAL_ACCOUNTS;

public class OfficialAccountViewModel extends ViewModel {
    private List<Task<QuerySnapshot>> tasks;

    public MutableLiveData<List<RoomItem>> liveOfficialAccounts;
    private static final String TAG = OfficialAccountViewModel.class.getSimpleName();

    public OfficialAccountViewModel() {
        liveOfficialAccounts = new MutableLiveData<>((List<RoomItem>) new ArrayList<RoomItem>());

        FirebaseFirestore firestore = ZaloApplication.getFirebaseInstance();

//        tasks = new ArrayList<>();
//
//        //get roomIds
//        tasks.add(firestore.collection(COLLECTION_BELONGS_TO)
//                .whereEqualTo("userPhone", ZaloApplication.currentUserPhone)
//                .get());
//
//        //get all rooms
//        tasks.add(firestore.collection(COLLECTION_ROOMS)
//                .get());
//
//        Tasks.whenAll(tasks)
//                .addOnCompleteListener(new OfficialAccountViewModel.OfficialAccountQueryListener());
//        firestore.collection(COLLECTION_OFFICIAL_ACCOUNTS).get().addOnCompleteListener(new OfficialAccountQueryListener());
    }

    class OfficialAccountQueryListener implements OnCompleteListener<QuerySnapshot> {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful()){
                // get rooms
                List<RoomItem> rooms = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    RoomItem roomItem = new RoomItem();
                    roomItem.name = doc.getString("name");
                    roomItem.avatar = doc.getString("avatar");

                    rooms.add(roomItem);
                }

                liveOfficialAccounts.setValue(rooms);
            } else {
                Log.d(TAG, "OfficialAccount Query fail");
            }
        }
    }
}
