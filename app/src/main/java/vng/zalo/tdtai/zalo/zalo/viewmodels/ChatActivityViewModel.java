package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.app.Application;
import android.content.Intent;
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
import vng.zalo.tdtai.zalo.zalo.models.MessageModel;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_MESSAGES;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_AVATAR;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_ID;

public class ChatActivityViewModel extends ViewModel {
    private Intent intent;
    private static final String TAG = ChatActivityViewModel.class.getSimpleName();
    private FirebaseFirestore fireStore;
    public MutableLiveData<List<MessageModel>> liveDataMsgList;

    public ChatActivityViewModel(Intent intent, Application application){
        fireStore = ((ZaloApplication)application).mFireStore;

        liveDataMsgList = new MutableLiveData<>((List<MessageModel>)new ArrayList<MessageModel>());

        this.intent = intent;
        Long roomId = intent.getLongExtra(ROOM_ID,-1);
        Log.d(TAG,"roomId: "+roomId);

        fireStore.collection(COLLECTION_MESSAGES)
                .whereEqualTo("roomId",roomId)
                .orderBy("id")
                .get()
                .addOnCompleteListener(new MessagesQueryListener());
    }

    class MessagesQueryListener implements OnCompleteListener<QuerySnapshot>{

        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful()){
                Log.d(TAG,"MessagesQuery successful\n"+task.getException());
                List<MessageModel> messages = new ArrayList<>();
                for(QueryDocumentSnapshot doc: task.getResult()){
                    MessageModel message = new MessageModel();
                    message.id = doc.getLong("id");
                    message.content = doc.getString("content");
                    message.avatar = intent.getStringExtra(ROOM_AVATAR);
                    message.senderPhone = doc.getString("senderPhone");

                    messages.add(message);
                }
                liveDataMsgList.setValue(messages);
            } else {
                Log.d(TAG,"MessagesQuery unsuccessful\n"+task.getException());
            }
        }
    }
}