package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import javax.inject.Inject;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity.DaggerRoomActivityComponent;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity.RoomActivityModule;
import vng.zalo.tdtai.zalo.zalo.models.Message;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;
import vng.zalo.tdtai.zalo.zalo.utils.MessageDiffCallback;
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

        recyclerView.setLayoutManager(layoutManager);

        viewModel.liveMessages.observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messageList) {
                adapter.submitList(messageList, new Runnable() {
                    @Override
                    public void run() {
                        scrollRecyclerViewToLastPosition();
                    }
                });
                Log.d(TAG, "onChanged liveData");
            }
        });

        adapter = new RoomActivityAdapter(this, new MessageDiffCallback());

        recyclerView.setAdapter(adapter);

        pictureImgButton = findViewById(R.id.pictureImgButton);
        sendMsgImgButton = findViewById(R.id.sendMsgImgButton);
        uploadFileImgButton = findViewById(R.id.uploadFileImgButton);
        voiceImgButton = findViewById(R.id.voiceImgButton);

        msgTextInputEditText = findViewById(R.id.msgTextInputEditText);
        msgTextInputEditText.addTextChangedListener(new InputTextListener());

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
                String textToSend = msgTextInputEditText.getText() == null ? "" : msgTextInputEditText.getText().toString();
                viewModel.addMessageToFirestore(textToSend);

                msgTextInputEditText.setText("");
                break;
        }
    }

    class InputTextListener implements TextWatcher {
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
                uploadFileImgButton.setVisibility(View.GONE);
                voiceImgButton.setVisibility(View.GONE);
                pictureImgButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    void scrollRecyclerViewToLastPosition() {
        recyclerView.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
    }
}