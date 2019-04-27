package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity.DaggerRoomActivityComponent;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity.RoomActivityModule;
import vng.zalo.tdtai.zalo.zalo.models.Message;
import vng.zalo.tdtai.zalo.zalo.models.RoomMember;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;
import vng.zalo.tdtai.zalo.zalo.utils.MessageModelDiffCallback;
import vng.zalo.tdtai.zalo.zalo.utils.Utils;
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = RoomActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private TextInputEditText msgTextInputEditText;
    private ImageView uploadFileImgButton;
    private ImageView voiceImgButton;
    private ImageView pictureImgButton;
    private ImageView sendMsgImgButton;

    @Inject
    RoomActivityViewModel viewModel;

    RoomActivityAdapter adapter;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DaggerRoomActivityComponent.builder()
                .roomActivityModule(new RoomActivityModule(this))
                .build().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Toolbar toolbar = findViewById(R.id.toolbarRoomActivity);
        toolbar.setTitle(getIntent().getStringExtra(Constants.ROOM_NAME));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.e(TAG, "actionBar is null");
        }

        recyclerView = findViewById(R.id.recyclerViewRoomActivity);

        layoutManager = new LinearLayoutManager(this);
//        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);

        viewModel.liveMessages.observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messageList) {
                adapter.submitList(messageList, new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.smoothScrollToPosition(Math.max(adapter.getItemCount()-1,0));
                    }
                });
                Log.d(TAG, "onChanged liveData");
            }
        });

        adapter = new RoomActivityAdapter(this, new MessageModelDiffCallback());

        recyclerView.setAdapter(adapter);
//        recyclerView.smoothScrollToPosition(Math.max(adapter.getItemCount()-1,0));

        pictureImgButton = findViewById(R.id.pictureImgButton);
        sendMsgImgButton = findViewById(R.id.sendMsgImgButton);
        uploadFileImgButton = findViewById(R.id.uploadFileImgButton);
        voiceImgButton = findViewById(R.id.voiceImgButton);

        msgTextInputEditText = findViewById(R.id.msgTextInputEditText);
        msgTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    sendMsgImgButton.setVisibility(View.GONE);
                    uploadFileImgButton.setVisibility(View.VISIBLE);
                    voiceImgButton.setVisibility(View.VISIBLE);
                    pictureImgButton.setVisibility(View.VISIBLE);
                } else {
                    sendMsgImgButton.setVisibility(View.VISIBLE);
                    uploadFileImgButton.setVisibility(View.INVISIBLE);
                    voiceImgButton.setVisibility(View.INVISIBLE);
                    pictureImgButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        sendMsgImgButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMsgImgButton:
                addMessageToFirestore();

                msgTextInputEditText.setText("");
                break;
        }
    }

    void addMessageToFirestore(){
        String textToSend = msgTextInputEditText.getText() == null ? "" : msgTextInputEditText.getText().toString();

        final Message message = new Message();
        message.content = textToSend;
        message.senderPhone = ZaloApplication.currentUserPhone;
        message.createdTime = Timestamp.now();

        ZaloApplication.getFirebaseInstance().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction){
                //add message
                ZaloApplication.getFirebaseInstance()
                        .collection(Constants.COLLECTION_ROOMS)
                        .document(viewModel.room.id)
                        .collection(Constants.COLLECTION_MESSAGES)
                        .document()
                        .set(message.toMap());

                //for all users in this room, change last message
                for(Map.Entry<String, RoomMember> entry :viewModel.room.memberMap.entrySet()){
                    String curUserPhone = entry.getKey();
                    String lastMsg = curUserPhone.equals(ZaloApplication.currentUserPhone)? "Tôi: ":(curUserPhone+": ");
                    lastMsg += message.content;
                    if(lastMsg.length() > Constants.MAX_TEXT_PREVIEW_LEN){
                        lastMsg = lastMsg.substring(0,Constants.MAX_TEXT_PREVIEW_LEN) + "...";
                    }

                    DocumentReference currentRoom = ZaloApplication.getFirebaseInstance()
                            .collection(Constants.COLLECTION_USERS)
                            .document(curUserPhone)
                            .collection(Constants.COLLECTION_ROOMS)
                            .document(viewModel.room.id);

                    currentRoom.get()
                            .addOnCompleteListener(new GetRoomQueryListener(currentRoom, lastMsg, message.createdTime));
                }

                return null;
            }
        });
    }

    class GetRoomQueryListener implements OnCompleteListener<DocumentSnapshot>{
        private DocumentReference currentRoom;
        private String lastMsg;
        private Timestamp lastMsgTime;

        GetRoomQueryListener(DocumentReference currentRoom, String lastMsg, Timestamp lastMsgTime){
            this.currentRoom = currentRoom;
            this.lastMsg = lastMsg;
            this.lastMsgTime = lastMsgTime;
        }
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if(task.isSuccessful()){
                Map<String, Object> newRoomItem = Utils.RoomItemDocumentToMap(task.getResult());
                newRoomItem.put("lastMsg",lastMsg);
                newRoomItem.put("lastMsgTime",lastMsgTime);
                Log.d(TAG, newRoomItem.toString());

                currentRoom.set(newRoomItem)
                        .addOnCompleteListener(new AddRoomQueryListener());
            } else {
                Log.d(TAG,"GetRoomQuery fail");
            }
        }
    }

    class AddRoomQueryListener implements OnCompleteListener<Void>{
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(!task.isSuccessful()){
                Log.d(TAG,"AddRoomQuery fail");
            }
        }
    }
}