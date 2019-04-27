package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomModel;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_BELONGS_TO;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_OFFICIAL_ACCOUNTS;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_ROOMS;

public class OfficialAccountViewModel extends ViewModel {
    private List<Task<QuerySnapshot>> tasks;

    public MutableLiveData<List<RoomModel>> liveOfficialAccounts;
    private static final String TAG = OfficialAccountViewModel.class.getSimpleName();

    public OfficialAccountViewModel() {
        liveOfficialAccounts = new MutableLiveData<>((List<RoomModel>) new ArrayList<RoomModel>());

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
        firestore.collection(COLLECTION_OFFICIAL_ACCOUNTS).get().addOnCompleteListener(new OfficialAccountQueryListener());
    }

    class OfficialAccountQueryListener implements OnCompleteListener<QuerySnapshot> {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful()){
                // get rooms
                List<RoomModel> rooms = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    RoomModel roomModel = new RoomModel();
                    roomModel.name = doc.getString("name");
                    roomModel.avatar = doc.getString("avatar");

                    rooms.add(roomModel);
                }

                liveOfficialAccounts.setValue(rooms);
            } else {
                Log.d(TAG, "OfficialAccount Query fail");
            }
        }
    }
}
