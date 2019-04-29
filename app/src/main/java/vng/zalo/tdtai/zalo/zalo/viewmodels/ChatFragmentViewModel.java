package vng.zalo.tdtai.zalo.zalo.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;

public class ChatFragmentViewModel extends ViewModel {
    private static final String TAG = ChatFragmentViewModel.class.getSimpleName();

    public MutableLiveData<List<RoomItem>> liveRoomItems;

    public ChatFragmentViewModel() {
        liveRoomItems = new MutableLiveData<>((List<RoomItem>) new ArrayList<RoomItem>());

        ZaloApplication.getFirebaseInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUserPhone)
                .collection(Constants.COLLECTION_ROOMS)
                .orderBy("lastMsgTime")
                .addSnapshotListener(new RoomEventListener());
    }

    class RoomEventListener implements EventListener<QuerySnapshot> {
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            List<RoomItem> roomItems = new ArrayList<>();
            for(DocumentSnapshot doc: queryDocumentSnapshots){
                RoomItem roomItem = doc.toObject(RoomItem.class);
                roomItem.roomId = doc.getId();
                roomItems.add(roomItem);
            }
            liveRoomItems.setValue(roomItems);
        }
    }
}