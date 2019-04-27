package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomModel;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_BELONGS_TO;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_IS_FRIEND_OF;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_ROOMS;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_USERS;

public class ChatFragmentViewModel extends ViewModel {
    private List<Task<QuerySnapshot>> tasks;

    public MutableLiveData<List<RoomModel>> liveRooms;
    private static final String TAG = ChatFragmentViewModel.class.getSimpleName();

    public ChatFragmentViewModel() {
        liveRooms = new MutableLiveData<>((List<RoomModel>) new ArrayList<RoomModel>());

        FirebaseFirestore firestore = ZaloApplication.getFirebaseInstance();

        tasks = new ArrayList<>();

        //get roomIds
        tasks.add(firestore.collection(COLLECTION_BELONGS_TO)
                .whereEqualTo("userPhone", ZaloApplication.currentUserPhone)
                .get());

        //get all rooms
        tasks.add(firestore.collection(COLLECTION_ROOMS)
                .get());

        //get all contacts
        tasks.add(firestore.collection(COLLECTION_IS_FRIEND_OF)
        .whereArrayContains("userPhones",ZaloApplication.currentUserPhone)
        .get());

        //get all users
        tasks.add(firestore.collection(COLLECTION_USERS)
                .get());

        Tasks.whenAll(tasks)
                .addOnCompleteListener(new RoomQueryListener());
    }

    class RoomQueryListener implements OnCompleteListener<Void> {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(task.isSuccessful()){
                // get roomIds
                List<String> roomIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : tasks.get(0).getResult()) {
                    roomIds.add(doc.getString("roomId"));
                }

                // get all rooms
                List<RoomModel> rooms = new ArrayList<>();
                for (QueryDocumentSnapshot doc : tasks.get(1).getResult()) {
                    String roomId = doc.getId();
                    Log.d(TAG,roomId);
                    if (roomIds.contains(roomId)) {
                        RoomModel room = new RoomModel();
                        room.id = roomId;
                        room.name = doc.getString("name");
                        String roomAvatar = doc.getString("avatar");
                        if (roomAvatar != null) {
                            room.avatar = roomAvatar;
                        }
                        room.lastMgsDate = doc.getDate("lastMsgDate");
                        rooms.add(room);
                    }
                }

                Map<String,String> userAvatars = new HashMap<>();
                for (QueryDocumentSnapshot doc : tasks.get(3).getResult()) {
                    userAvatars.put(doc.getString("phone"),doc.getString("avatar"));
                }

                for (QueryDocumentSnapshot doc : tasks.get(2).getResult()) {
                    List<String> userPhones = (List<String>) doc.get("userPhones");
                    for (String userPhone : userPhones) {
                        if (!userPhone.equals(ZaloApplication.currentUserPhone)) {
                            RoomModel room = new RoomModel();
                            room.name = userPhone;
                            String roomAvatar = userAvatars.get(userPhone);
                            if (roomAvatar != null) {
                                room.avatar = roomAvatar;
                            }
                            rooms.add(room);
                        }
                    }
                }

                liveRooms.setValue(rooms);
            } else {
                Log.d(TAG, "Room Query fail");
            }
        }
    }
}