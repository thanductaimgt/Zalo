package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.official_account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.viewmodels_factory.OfficialAccountViewModelFactory;
import vng.zalo.tdtai.zalo.zalo.models.RoomModel;
import vng.zalo.tdtai.zalo.zalo.utils.RoomModelDiffCallback;
import vng.zalo.tdtai.zalo.zalo.viewmodels.OfficialAccountViewModel;
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_AVATAR;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_ID;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_NAME;

public class OfficialAccountSubFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = OfficialAccountSubFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private OfficialAccountViewModel viewModel;
    private OfficialAccountAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sub_fragment_official_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, new OfficialAccountViewModelFactory()).get(OfficialAccountViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new OfficialAccountAdapter(this, new RoomModelDiffCallback());

        recyclerView.setAdapter(adapter);

        viewModel.liveOfficialAccounts.observe(getViewLifecycleOwner(), new Observer<List<RoomModel>>() {
            @Override
            public void onChanged(List<RoomModel> rooms) {
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
                intent.putExtra(ROOM_ID, adapter.getCurrentList().get(position).id);
                intent.putExtra(ROOM_NAME, adapter.getCurrentList().get(position).name);
                intent.putExtra(ROOM_AVATAR, adapter.getCurrentList().get(position).avatar);
                startActivity(intent);
        }
    }
}