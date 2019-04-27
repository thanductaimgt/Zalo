package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.content.Intent;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
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
import vng.zalo.tdtai.zalo.zalo.models.RoomMember;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;

public class RoomActivityViewModel extends ViewModel {
    private static final String TAG = RoomActivityViewModel.class.getSimpleName();

    private List<Task<QuerySnapshot>> tasks;

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

        tasks = new ArrayList<>();

        //get document
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

        //get messages
        Query getMessagesQuery = currentRoom.collection(Constants.COLLECTION_MESSAGES)
                .orderBy("createdTime");
        tasks.add(getMessagesQuery.get());
        //observe messages change
        getMessagesQuery.addSnapshotListener(new MessageEventListener());

        //get members
        tasks.add(currentRoom.collection(Constants.COLLECTION_MEMBERS)
                .get());

        Tasks.whenAll(tasks).addOnCompleteListener(new RoomQueryListener());
    }

    class MessageEventListener implements EventListener<QuerySnapshot>{
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            updateLiveMessagesValue(queryDocumentSnapshots);
        }
    }

    class RoomQueryListener implements OnCompleteListener<Void>{
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(task.isSuccessful()){
                //save members in room
                Map<String, RoomMember> memberMap = new HashMap<>();
                for(DocumentSnapshot doc: tasks.get(1).getResult()){
                    memberMap.put(doc.getString("phone"), RoomMember.docToRoomMember(doc));
                }
                room.memberMap = memberMap;

                //set livedata value
                updateLiveMessagesValue(tasks.get(0).getResult());
            } else {
                Log.d(TAG,"RoomQuery fail");
            }
        }
    }

    void updateLiveMessagesValue(QuerySnapshot docs){
        //set livedata value
        if(room.memberMap!=null){
            List<Message> messages = new ArrayList<>();
            for(DocumentSnapshot doc: docs){
                messages.add(Message.docToMessage(doc, room.memberMap));
            }
            liveMessages.setValue(messages);
        }
    }
}