package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.content.Intent;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.Message;
import vng.zalo.tdtai.zalo.zalo.models.Room;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.models.RoomMember;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;

public class RoomActivityViewModel extends ViewModel {
    private static final String TAG = RoomActivityViewModel.class.getSimpleName();
    private QuerySnapshot lastMessagesQuerySnapshot;

    private Room room;
    public MutableLiveData<List<Message>> liveMessages;

    public RoomActivityViewModel(Intent intent) {
        FirebaseFirestore firestore = ZaloApplication.getFirebaseInstance();

        liveMessages = new MutableLiveData<>(new ArrayList<>());

        room = new Room();
        room.avatar = intent.getStringExtra(Constants.ROOM_AVATAR);
        room.id = intent.getStringExtra(Constants.ROOM_ID);
        room.name = intent.getStringExtra(Constants.ROOM_NAME);

        //create current room reference
        DocumentReference currentRoom = firestore.collection(Constants.COLLECTION_ROOMS)
                .document(room.id);

        //get current room
        currentRoom.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                room.createdTime = Objects.requireNonNull(task.getResult()).getTimestamp("createdTime");
            } else {
                Log.d(TAG, "Get document fail");
            }
        });

        //observe messages change
        currentRoom.collection(Constants.COLLECTION_MESSAGES)
                .orderBy("createdTime")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    updateLiveMessagesValue(queryDocumentSnapshots);
                    lastMessagesQuerySnapshot = queryDocumentSnapshots;
                });

        //get members
        currentRoom.collection(Constants.COLLECTION_MEMBERS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //save members in room
                        Map<String, RoomMember> memberMap = new HashMap<>();
                        for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                            RoomMember roomMember = doc.toObject(RoomMember.class);
                            assert roomMember != null;
                            memberMap.put(Objects.requireNonNull(doc.getString("phone")), roomMember);
                        }
                        room.memberMap = memberMap;

                        //set livedata value
                        updateLiveMessagesValue(lastMessagesQuerySnapshot);
                    } else {
                        Log.d(TAG, "RoomQuery fail");
                    }
                });

        //observe to set unseenMsgNum = 0
        DocumentReference thisRoomItem = ZaloApplication.getFirebaseInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUser.phone)
                .collection(Constants.COLLECTION_ROOMS)
                .document(room.id);

        String currentUserPhone = ZaloApplication.currentUser.phone;
        thisRoomItem.addSnapshotListener((documentSnapshot, e) -> {
            assert documentSnapshot != null;
            RoomItem roomItem = documentSnapshot.toObject(RoomItem.class);
            assert roomItem != null;
            if (roomItem.unseenMsgNum > 0 && currentUserPhone.equals(ZaloApplication.currentUser.phone)) {
                Log.d(TAG, "onEvent set unseen = 0 of " + thisRoomItem.getPath());
                roomItem.unseenMsgNum = (long) 0;
                thisRoomItem.set(roomItem.toMap())
                        .addOnFailureListener(e1 -> Log.d(TAG, "Change unseenMsgNum = 0 fail"));
            }
        });
    }

    private void updateLiveMessagesValue(QuerySnapshot docs) {
        //set livedata value
        if (docs != null) {
            List<Message> messages = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                Message message = doc.toObject(Message.class);
                if (room.memberMap != null) {
                    assert message != null;
                    message.senderAvatar = Objects.requireNonNull(room.memberMap.get(message.senderPhone)).avatar;
                }
                messages.add(message);
            }
            liveMessages.setValue(messages);
        }
    }

    public void addMessageToFirestore(String content) {
        final Message message = new Message();
        message.content = content;
        message.senderPhone = ZaloApplication.currentUser.phone;
        message.createdTime = Timestamp.now();

        //add message
        ZaloApplication.getFirebaseInstance()
                .collection(Constants.COLLECTION_ROOMS)
                .document(room.id)
                .collection(Constants.COLLECTION_MESSAGES)
                .document()
                .set(message.toMap());

        //for all users in this room except the current user, change last message
        for (Map.Entry<String, RoomMember> entry : room.memberMap.entrySet()) {
            String curUserPhone = entry.getKey();
            String lastMsg = (curUserPhone.equals(ZaloApplication.currentUser.phone) ? Constants.ME : ZaloApplication.currentUser.phone) + ": " + message.content;

            DocumentReference currentRoom = ZaloApplication.getFirebaseInstance()
                    .collection(Constants.COLLECTION_USERS)
                    .document(curUserPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(room.id);

            currentRoom.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, Object> newRoomItem = Objects.requireNonNull(task.getResult()).getData();
                            assert newRoomItem != null;
                            newRoomItem.put("lastMsg", lastMsg);
                            newRoomItem.put("lastMsgTime", message.createdTime);
                            if (!curUserPhone.equals(ZaloApplication.currentUser.phone))
                                newRoomItem.put("unseenMsgNum", (long) newRoomItem.get("unseenMsgNum") + 1);

                            currentRoom.set(newRoomItem)
                                    .addOnFailureListener(e -> Log.d(TAG, "Update last message fail"));
                        } else {
                            Log.d(TAG, "GetRoomQuery fail");
                        }
                    });
        }
    }
}