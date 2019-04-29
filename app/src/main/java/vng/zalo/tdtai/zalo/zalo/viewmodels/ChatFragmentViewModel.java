package vng.zalo.tdtai.zalo.zalo.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;

public class ChatFragmentViewModel extends ViewModel {
    private static final String TAG = ChatFragmentViewModel.class.getSimpleName();

    public MutableLiveData<List<RoomItem>> liveRoomItems;

    public ChatFragmentViewModel() {
        liveRoomItems = new MutableLiveData<>(new ArrayList<>());

        ZaloApplication.getFirebaseInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUser.phone)
                .collection(Constants.COLLECTION_ROOMS)
                .orderBy("lastMsgTime", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    List<RoomItem> roomItems = new ArrayList<>();
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        RoomItem roomItem = doc.toObject(RoomItem.class);
                        assert roomItem != null;
                        roomItem.roomId = doc.getId();
                        roomItems.add(roomItem);
                    }
                    liveRoomItems.setValue(roomItems);
                });
    }
}