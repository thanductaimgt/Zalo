package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;

public class ContactSubFragmentViewModel extends ViewModel {
    public MutableLiveData<List<RoomItem>> liveContacts;
    private static final String TAG = ContactSubFragmentViewModel.class.getSimpleName();

    public ContactSubFragmentViewModel() {
        liveContacts = new MutableLiveData<>(new ArrayList<>());

        ZaloApplication.getFirebaseInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUser.phone)
                .collection(Constants.COLLECTION_CONTACTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RoomItem> roomItems = new ArrayList<>();
                        for(DocumentSnapshot doc: Objects.requireNonNull(task.getResult())){
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
                });
    }
}