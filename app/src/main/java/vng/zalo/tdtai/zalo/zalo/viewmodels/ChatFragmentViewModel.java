package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomModel;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_BELONGS_TO;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_ROOMS;

public class ChatFragmentViewModel extends ViewModel {
    private FirebaseFirestore fireStore;
    public MutableLiveData<List<RoomModel>> liveRooms;
    private static final String TAG = ChatFragmentViewModel.class.getSimpleName();

    public ChatFragmentViewModel(Application application) {
        liveRooms = new MutableLiveData<>((List<RoomModel>) new ArrayList<RoomModel>());

        fireStore = ((ZaloApplication) application).mFireStore;

        fireStore.collection(COLLECTION_BELONGS_TO)
                .whereEqualTo("userPhone", ZaloApplication.sCurrentUserPhone)
                .get()
                .addOnCompleteListener(new RoomIdsQueryListener());
    }

    class RoomIdsQueryListener implements OnCompleteListener<QuerySnapshot> {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                List<Long> roomIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    roomIds.add(doc.getLong("roomId"));
                }
                fireStore.collection(COLLECTION_ROOMS)
                        .get()
                        .addOnCompleteListener(new RoomsQueryListener(roomIds));
            } else {
                Log.d(TAG,"RoomIdsQuery unsuccessful");
            }
        }
    }

    class RoomsQueryListener implements OnCompleteListener<QuerySnapshot> {
        private List<Long> roomIds;
        RoomsQueryListener(List<Long> roomIds){
            this.roomIds = roomIds;
        }

        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                List<RoomModel> rooms = new ArrayList<>();
                for(QueryDocumentSnapshot doc: task.getResult()){
                    Long roomId = doc.getLong("id");
                    if(roomIds.contains(roomId)){
                        RoomModel room = new RoomModel();
                        room.id = roomId;
                        room.name = doc.getString("name");
                        String roomAvatar = doc.getString("avatar");
                        if(roomAvatar != null){
                            room.avatar = roomAvatar;
                        }
                        rooms.add(room);
                    }
                }
                liveRooms.setValue(rooms);
            } else {
                Log.d(TAG,"RoomsQuery unsuccessful");
            }
        }
    }
}