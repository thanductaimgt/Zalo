package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.dependency_factories.viewmodels_factory.GroupFragmentViewModelFactory;
import vng.zalo.tdtai.zalo.zalo.utils.RoomItemDiffCallback;
import vng.zalo.tdtai.zalo.zalo.viewmodels.GroupFragmentViewModel;
import vng.zalo.tdtai.zalo.zalo.adapters.RoomItemAdapter;
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity;
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.CreateGroupActivity;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_AVATAR;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_ID;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_NAME;

public class GroupFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final String TAG = GroupFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private GroupFragmentViewModel viewModel;
    private RoomItemAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this, new GroupFragmentViewModelFactory()).get(GroupFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerViewGroupFragment);

        adapter = new RoomItemAdapter(this, new RoomItemDiffCallback());//GroupFragmentAdapter(this, new RoomItemDiffCallback());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setHasFixedSize(true);

        viewModel.liveRoomItems.observe(getViewLifecycleOwner(), rooms -> {
            adapter.submitList(rooms, () -> Log.d(TAG, Objects.requireNonNull(viewModel.liveRoomItems.getValue()).toString()));
            Log.d(TAG,"onChanged livedata");
            Log.d(TAG, rooms.toString());
        });

        View createNewGroupLayout = view.findViewById(R.id.createNewGroupLayout);
        createNewGroupLayout.setOnClickListener(this);

        Spinner spinner = view.findViewById(R.id.sortGroupSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()),
                R.array.sort_group_options, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
            case R.id.createNewGroupLayout:
                startActivity(new Intent(getActivity(), CreateGroupActivity.class));
        }
    }
}