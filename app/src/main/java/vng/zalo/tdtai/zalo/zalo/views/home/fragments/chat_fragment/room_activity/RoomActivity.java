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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity.DaggerRoomActivityComponent;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity.RoomActivityModule;
import vng.zalo.tdtai.zalo.zalo.models.MessageModel;
import vng.zalo.tdtai.zalo.zalo.utils.MessageModelDiffCallback;
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_MESSAGES;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_ROOMS;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_AVATAR;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_ID;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_NAME;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = RoomActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private TextInputEditText msgTextInputEditText;
    private ImageView uploadFileImgButton;
    private ImageView voiceImgButton;
    private ImageView pictureImgButton;
    private ImageView sendMsgImgButton;

    private String roomId;

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
        toolbar.setTitle(getIntent().getStringExtra(ROOM_NAME));
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

//        viewModel = ViewModelProviders.of(this, new RoomActivityViewModelFactory(getIntent(), getApplication()))
//                .get(RoomActivityViewModel.class);

        adapter = new RoomActivityAdapter(this, new MessageModelDiffCallback());
        viewModel.liveMessages.observe(this, new Observer<List<MessageModel>>() {
            @Override
            public void onChanged(List<MessageModel> messageModelList) {
                adapter.submitList(messageModelList);
                Log.d(TAG, "onChanged liveData");
            }
        });

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

        roomId = getIntent().getStringExtra(ROOM_ID);
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
                String textToSend = msgTextInputEditText.getText() == null ? "" : msgTextInputEditText.getText().toString();
                final List<MessageModel> msgList = viewModel.liveMessages.getValue();
                if (msgList != null) {
                    final MessageModel message = new MessageModel();
                    message.id = (long) msgList.size();
                    message.content = textToSend;

                    final Map<String, Object> newMessage = new HashMap<>();
                    newMessage.put("content", message.content);
                    newMessage.put("createdTime", message.createdTime);
                    newMessage.put("id", message.id);
                    newMessage.put("senderPhone", message.senderPhone);

                    if (roomId != null) {
                        newMessage.put("roomId", roomId);

                        addMessage(newMessage, msgList, message);
                    } else {
                        Map<String, Object> newRoom = new HashMap<>();
                        newRoom.put("avatar", getIntent().getStringExtra(ROOM_AVATAR));
                        newRoom.put("lastMsgDate", new Date());
                        newRoom.put("name", "");

                        final DocumentReference newRoomDocument = ZaloApplication.getFirebaseInstance()
                                .collection(COLLECTION_ROOMS)
                                .document();

                        newRoomDocument.set(newRoom)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            roomId = newRoomDocument.getId();
                                            newMessage.put("roomId", roomId);

                                            addMessage(newMessage, msgList, message);
                                        } else {
                                            Log.d(TAG, "Fail to create new room");
                                        }
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "liveData value is null");
                }

                msgTextInputEditText.setText("");
                break;
        }
    }

    void addMessage(Map<String, Object> newMessage, final List<MessageModel> msgList, final MessageModel message) {
        ZaloApplication.getFirebaseInstance()
                .collection(COLLECTION_MESSAGES)
                .document()
                .set(newMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        msgList.add(message);
                        viewModel.liveMessages.setValue(msgList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Fail to insert message to fireBase");
                    }
                });
    }
}