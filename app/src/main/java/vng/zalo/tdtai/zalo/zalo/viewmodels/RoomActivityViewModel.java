package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.content.Intent;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import javax.annotation.Nullable;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.Message;
import vng.zalo.tdtai.zalo.zalo.models.Room;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.models.RoomMember;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity;

public class RoomActivityViewModel extends ViewModel {
    private static final String TAG = RoomActivityViewModel.class.getSimpleName();
    private QuerySnapshot lastMessagesQuerySnapshot;

    public Room room;
    public MutableLiveData<List<Message>> liveMessages;

    public RoomActivityViewModel(Intent intent){
        FirebaseFirestore firestore = ZaloApplication.getFirebaseInstance();

        liveMessages = new MutableLiveData<>((List<Message>)new ArrayList<Message>());

        room = new Room();
        room.avatar = intent.getStringExtra(Constants.ROOM_AVATAR);
        room.id = intent.getStringExtra(Constants.ROOM_ID);
        room.name = intent.getStringExtra(Constants.ROOM_NAME);

        //create current room reference
        DocumentReference currentRoom = firestore.collection(Constants.COLLECTION_ROOMS)
                .document(room.id);

        //get current room
        currentRoom.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    room.createdTime = task.getResult().getTimestamp("createdTime");
                } else {
                    Log.d(TAG,"Get document fail");
                }
            }
        });

        //observe messages change
        currentRoom.collection(Constants.COLLECTION_MESSAGES)
                .orderBy("createdTime")
                .addSnapshotListener(new MessageEventListener());

        //get members
        currentRoom.collection(Constants.COLLECTION_MEMBERS)
                .get()
                .addOnCompleteListener(new RoomMembersQueryListener());

        //observe to set unseenMsgNum = 0
        DocumentReference thisRoomItem = ZaloApplication.getFirebaseInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUserPhone)
                .collection(Constants.COLLECTION_ROOMS)
                .document(room.id);

        thisRoomItem.addSnapshotListener(new RoomItemListener(thisRoomItem, ZaloApplication.currentUserPhone));
        Log.d(TAG,thisRoomItem.getPath());
    }

    class RoomItemListener implements EventListener<DocumentSnapshot>{
        private DocumentReference thisRoomItem;
        private String currentUserPhone;

        RoomItemListener(DocumentReference thisRoomItem, String currentUserPhone){
            this.thisRoomItem = thisRoomItem;
            this.currentUserPhone = currentUserPhone;
        }

        @Override
        public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
            RoomItem roomItem = doc.toObject(RoomItem.class);
            if(roomItem.unseenMsgNum > 0 && currentUserPhone.equals(ZaloApplication.currentUserPhone)){
                Log.d(TAG,"onEvent set unseen = 0 of "+thisRoomItem.getPath());
                roomItem.unseenMsgNum = (long)0;
                thisRoomItem.set(roomItem.toMap())
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"Change unseenMsgNum = 0 fail");
                            }
                        });
            }
        }
    }

    class MessageEventListener implements EventListener<QuerySnapshot>{
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            updateLiveMessagesValue(queryDocumentSnapshots);
            lastMessagesQuerySnapshot = queryDocumentSnapshots;
        }
    }

    class RoomMembersQueryListener implements OnCompleteListener<QuerySnapshot>{
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful()){
                //save members in room
                Map<String, RoomMember> memberMap = new HashMap<>();
                for(DocumentSnapshot doc: task.getResult()){
                    RoomMember roomMember = doc.toObject(RoomMember.class);
                    memberMap.put(doc.getString("phone"), roomMember);
                }
                room.memberMap = memberMap;

                //set livedata value
                updateLiveMessagesValue(lastMessagesQuerySnapshot);
            } else {
                Log.d(TAG,"RoomQuery fail");
            }
        }
    }

    private void updateLiveMessagesValue(QuerySnapshot docs){
        //set livedata value
        if(docs != null){
            List<Message> messages = new ArrayList<>();
            for(DocumentSnapshot doc: docs){
                Message message = doc.toObject(Message.class);
                if(room.memberMap != null){
                    message.senderAvatar = room.memberMap.get(message.senderPhone).avatar;
                }
                messages.add(message);
            }
            liveMessages.setValue(messages);
        }
    }

    public void addMessageToFirestore(String textToSend) {
        final Message message = new Message();
        message.content = textToSend;
        message.senderPhone = ZaloApplication.currentUserPhone;
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
            String lastMsg = (curUserPhone.equals(ZaloApplication.currentUserPhone) ? Constants.ME : ZaloApplication.currentUserPhone) + ": ";
            lastMsg += message.content;

            DocumentReference currentRoom = ZaloApplication.getFirebaseInstance()
                    .collection(Constants.COLLECTION_USERS)
                    .document(curUserPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(room.id);

            boolean increaseUnseenMsgNum = !curUserPhone.equals(ZaloApplication.currentUserPhone);
            currentRoom.get()
                    .addOnCompleteListener(new GetRoomQueryListener(currentRoom, lastMsg, message.createdTime, increaseUnseenMsgNum));
        }
    }

    class GetRoomQueryListener implements OnCompleteListener<DocumentSnapshot>{
        private DocumentReference currentRoom;
        private String lastMsg;
        private Timestamp lastMsgTime;
        private boolean increaseUnseenMsgNum;

        GetRoomQueryListener(DocumentReference currentRoom, String lastMsg, Timestamp lastMsgTime, boolean increaseUnseenMsgNum){
            this.currentRoom = currentRoom;
            this.lastMsg = lastMsg;
            this.lastMsgTime = lastMsgTime;
            this.increaseUnseenMsgNum = increaseUnseenMsgNum;
        }
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if(task.isSuccessful()){
                Map<String, Object> newRoomItem = task.getResult().getData();
                newRoomItem.put("lastMsg",lastMsg);
                newRoomItem.put("lastMsgTime",lastMsgTime);
                if(increaseUnseenMsgNum)
                    newRoomItem.put("unseenMsgNum",(long)newRoomItem.get("unseenMsgNum")+1);

                Log.d(TAG, newRoomItem.toString());

                currentRoom.set(newRoomItem)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"Update last message fail");
                            }
                        });
            } else {
                Log.d(TAG,"GetRoomQuery fail");
            }
        }
    }
}