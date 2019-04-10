package vng.zalo.tdtai.zalo.zalo.views.lobby.fragments.chat_fragment.chat_activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.multidex.MultiDexApplication;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.dependencyfactories.ChatActivityFactory;
import vng.zalo.tdtai.zalo.zalo.models.MessageModel;
import vng.zalo.tdtai.zalo.zalo.utils.MessageModelDiffCallback;
import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatActivityViewModel;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import javax.inject.Inject;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.CHAT_FRAGMENT_PEER_NAME;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = ChatActivity.class.getSimpleName();
    @Inject
    MessageModelDiffCallback messageModelDiffCallback;

    private RecyclerView recyclerView;
    private TextInputEditText msgTextInputEditText;
    private ImageButton uploadFileImgButton;
    private ImageButton voiceImgButton;
    private ImageButton pictureImgButton;
    private ImageButton sendMsgImgButton;

    ChatActivityViewModel viewModel;
    ChatActivityAdapter adapter;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle(getIntent().getStringExtra(CHAT_FRAGMENT_PEER_NAME));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.e(TAG,"actionBar is null");
        }

        recyclerView = findViewById(R.id.chatActivityRecyclerView);

        layoutManager = new LinearLayoutManager(this);
//        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        viewModel = ViewModelProviders.of(this, new ChatActivityFactory(getApplication())).get(ChatActivityViewModel.class);

        adapter = new ChatActivityAdapter(messageModelDiffCallback);
        viewModel.liveDataMsgList.observe(this, new Observer<List<MessageModel>>() {
            @Override
            public void onChanged(List<MessageModel> messageModelList) {
                adapter.submitList(messageModelList);
                Log.d(TAG,"onChanged livedata");
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(Math.max(adapter.getItemCount()-1,0));

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
                if(s.length() == 0){
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
        getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
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
        switch (v.getId()){
            case R.id.sendMsgImgButton:
                String textToSend = msgTextInputEditText.getText() == null? "" : msgTextInputEditText.getText().toString();
                List<MessageModel> msgList = viewModel.liveDataMsgList.getValue();
                if(msgList != null){
                    msgList.add(new MessageModel(1,1998,textToSend,999999999,""));
                } else {
                    Log.e(TAG, "livedata value is null");
                }

                msgTextInputEditText.setText("");
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
                break;
        }
    }
}