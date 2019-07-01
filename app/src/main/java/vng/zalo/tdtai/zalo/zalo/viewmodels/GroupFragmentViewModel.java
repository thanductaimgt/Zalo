package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;

public class GroupFragmentViewModel extends ViewModel {
    private static final String TAG = GroupFragmentViewModel.class.getSimpleName();

    public MutableLiveData<List<RoomItem>> liveRoomItems;

    public GroupFragmentViewModel() {
        liveRoomItems = new MutableLiveData<>(new ArrayList<>());

        Log.d(TAG, ZaloApplication.currentUser.toString());

        ZaloApplication.getFirebaseInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUser.phone)
                .collection(Constants.COLLECTION_ROOMS)
                .whereEqualTo("roomType", Constants.ROOM_TYPE_GROUP)
                .orderBy("lastMsgTime")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null){
                        List<RoomItem> roomItems = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            RoomItem roomItem = doc.toObject(RoomItem.class);
                            assert roomItem != null;
                            roomItem.roomId = doc.getId();
                            roomItems.add(roomItem);
                        }
                        liveRoomItems.setValue(roomItems);
                        Log.d(TAG, liveRoomItems.getValue().toString());
                    } else {
                        Log.d(TAG, Constants.COLLECTION_USERS+"/"+ZaloApplication.currentUser.phone+"/"+Constants.COLLECTION_ROOMS);
                        Log.d(TAG, "queryDocumentSnapshots is null");
                    }
                });
    }
}