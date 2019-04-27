package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment;

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
import vng.zalo.tdtai.zalo.zalo.dependency_factories.viewmodels_factory.ChatFragmentViewModelFactory;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.RoomModelDiffCallback;
import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatFragmentViewModel;
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_AVATAR;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_ID;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_NAME;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = ChatFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private ChatFragmentViewModel viewModel;
    private ChatFragmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, new ChatFragmentViewModelFactory()).get(ChatFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.chatFragmentRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new ChatFragmentAdapter(this, new RoomModelDiffCallback());

        recyclerView.setAdapter(adapter);

        viewModel.liveRoomItems.observe(getViewLifecycleOwner(), new Observer<List<RoomItem>>() {
            @Override
            public void onChanged(List<RoomItem> rooms) {
                adapter.submitList(rooms);
                Log.d(TAG,"onChanged livedata");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.itemChatFragmentConstraintLayout:
                int position = recyclerView.getChildLayoutPosition(v);
                Intent intent = new Intent(getActivity(), RoomActivity.class);
                //room not created in database
                if(adapter.getCurrentList().get(position).id != null)
                    intent.putExtra(ROOM_ID, adapter.getCurrentList().get(position).id);

                intent.putExtra(ROOM_NAME, adapter.getCurrentList().get(position).name);
                intent.putExtra(ROOM_AVATAR, adapter.getCurrentList().get(position).avatar);
                startActivity(intent);
        }
    }
}