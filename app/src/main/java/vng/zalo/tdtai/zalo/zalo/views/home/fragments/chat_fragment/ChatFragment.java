package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.adapters.RoomItemAdapter;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.viewmodels_factory.ChatFragmentViewModelFactory;
import vng.zalo.tdtai.zalo.zalo.utils.RoomItemDiffCallback;
import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatFragmentViewModel;
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_AVATAR;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_ID;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_NAME;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = ChatFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private ChatFragmentViewModel viewModel;
    private RoomItemAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, new ChatFragmentViewModelFactory()).get(ChatFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.chatFragmentRecyclerView);

        adapter = new RoomItemAdapter(this, new RoomItemDiffCallback());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        viewModel.liveRoomItems.observe(getViewLifecycleOwner(), rooms -> {
            adapter.submitList(rooms, () -> Log.d(TAG, Objects.requireNonNull(viewModel.liveRoomItems.getValue()).toString()));
            Log.d(TAG,"onChanged livedata");
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.itemRoomRootLayout:
                int position = recyclerView.getChildLayoutPosition(v);
                Intent intent = new Intent(getActivity(), RoomActivity.class);
                //room not created in database
                if(adapter.getCurrentList().get(position).roomId != null)
                    intent.putExtra(ROOM_ID, adapter.getCurrentList().get(position).roomId);

                intent.putExtra(ROOM_NAME, adapter.getCurrentList().get(position).name);
                intent.putExtra(ROOM_AVATAR, adapter.getCurrentList().get(position).avatar);
                startActivity(intent);
        }
    }
}