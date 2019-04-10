package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.ChatItemModel;

public class ChatFragmentViewModel extends ViewModel {
    private FirebaseFirestore firestore;
    public MutableLiveData<List<ChatItemModel>> liveChatItemList;
    private static final String TAG = ChatFragmentViewModel.class.getSimpleName();

    public ChatFragmentViewModel(Application application){
        liveChatItemList = new MutableLiveData<>((List<ChatItemModel>) new ArrayList<ChatItemModel>());

        firestore = ((ZaloApplication)application).mFireStore;
        CollectionReference usersCollection = firestore.collection("users");

        //query data
        usersCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<ChatItemModel> chatItemList = new ArrayList<>();
                    for(QueryDocumentSnapshot document: task.getResult()){
                        chatItemList.add(document.toObject(ChatItemModel.class));
                        Log.d(TAG,"Query successfully");
                    }
                    liveChatItemList.setValue(chatItemList);
                } else {
                    Log.d(TAG,"Query unsuccessfully");
                }
            }
        });

        //on data set change
        usersCollection
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<ChatItemModel> chatItemList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("id") != null) {
                                chatItemList.add(doc.toObject(ChatItemModel.class));
                            }
                        }
                        liveChatItemList.setValue(chatItemList);
                    }
                });
    }
}