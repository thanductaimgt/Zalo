package vng.zalo.tdtai.zalo.zalo.views.lobby.fragments.chat_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.dependencyfactories.ChatFragmentViewModelFactory;
import vng.zalo.tdtai.zalo.zalo.models.ChatItemModel;
import vng.zalo.tdtai.zalo.zalo.utils.ChatItemDiffCallback;
import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatFragmentViewModel;
import vng.zalo.tdtai.zalo.zalo.views.lobby.fragments.chat_fragment.chat_activity.ChatActivity;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.CHAT_FRAGMENT_ITEM_POSI;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.CHAT_FRAGMENT_PEER_NAME;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = ChatFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private ChatFragmentViewModel viewModel;
    private ChatFragmentAdapter adapter;

//    @Inject
    ChatItemDiffCallback chatItemDiffCallback = new ChatItemDiffCallback();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, new ChatFragmentViewModelFactory(getActivity().getApplication())).get(ChatFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.chatFragmentRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new ChatFragmentAdapter(this, chatItemDiffCallback);

        recyclerView.setAdapter(adapter);

        viewModel.liveChatItemList.observe(getViewLifecycleOwner(), new Observer<List<ChatItemModel>>() {
            @Override
            public void onChanged(List<ChatItemModel> chatItemList) {
                adapter.submitList(chatItemList);
                Log.d(TAG,"onChanged livedata");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chatFragmentConstrLayout:
                int position = recyclerView.getChildLayoutPosition(v);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(CHAT_FRAGMENT_ITEM_POSI, adapter.getCurrentList().get(position).id);
                intent.putExtra(CHAT_FRAGMENT_PEER_NAME, adapter.getCurrentList().get(position).name);
                startActivity(intent);
        }
    }
}