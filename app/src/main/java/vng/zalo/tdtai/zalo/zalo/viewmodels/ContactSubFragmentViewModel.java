package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_USERS;

public class ContactSubFragmentViewModel extends ViewModel {
    public MutableLiveData<List<RoomItem>> liveContacts;
    private static final String TAG = ContactSubFragmentViewModel.class.getSimpleName();

    public ContactSubFragmentViewModel() {
        liveContacts = new MutableLiveData<>((List<RoomItem>) new ArrayList<RoomItem>());

        ZaloApplication.getFirebaseInstance()
                .collection(COLLECTION_USERS)
                .document(ZaloApplication.currentUserPhone)
                .collection(Constants.COLLECTION_CONTACTS)
                .get()
                .addOnCompleteListener(new ContactQueryListener());
    }

    class ContactQueryListener implements OnCompleteListener<QuerySnapshot> {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                List<RoomItem> roomItems = new ArrayList<>();
                for(DocumentSnapshot doc: task.getResult()){
                    RoomItem roomItem = new RoomItem();

                    roomItem.avatar = doc.getString("avatar");
                    roomItem.name = doc.getId();
                    roomItem.roomId = doc.getString("roomId");

                    roomItems.add(roomItem);
                }
                liveContacts.setValue(roomItems);
            } else {
                Log.d(TAG, "Contact Query Fail");
            }
        }
    }
}