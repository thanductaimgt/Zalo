package vng.zalo.tdtai.zalo.screens.lobby.fragments.chat_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.screens.lobby.fragments.chat_fragment.chat_activity.ChatActivity;

import static vng.zalo.tdtai.zalo.screens.utils.Constants.CHAT_FRAGMENT_ITEM_POSI;
import static vng.zalo.tdtai.zalo.screens.utils.Constants.CHAT_FRAGMENT_PEER_NAME;

public class ChatFragment extends Fragment implements View.OnClickListener{
    private RecyclerView recyclerView;
    private ChatFragmentViewModel viewModel;
    private ChatFragmentAdapter chatFragmentAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ChatFragmentViewModel();

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = root.findViewById(R.id.chatFragmentRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatFragmentAdapter = new ChatFragmentAdapter(viewModel.chatDataItemList.getValue(),this);
        recyclerView.setAdapter(chatFragmentAdapter);

        viewModel.chatDataItemList.observe(this, new Observer<List<ChatItemModel>>() {
            @Override
            public void onChanged(List<ChatItemModel> chatItemModels) {
                chatFragmentAdapter.updateChanges(chatItemModels);
            }
        });

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chatFragmentConstrLayout:
                int position = recyclerView.getChildLayoutPosition(v);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(CHAT_FRAGMENT_ITEM_POSI,chatFragmentAdapter.dataItemList.get(position).id);
                intent.putExtra(CHAT_FRAGMENT_PEER_NAME,chatFragmentAdapter.dataItemList.get(position).name);
                startActivity(intent);
        }
    }
}