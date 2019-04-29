package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.contacts;

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
import vng.zalo.tdtai.zalo.zalo.dependency_factories.viewmodels_factory.ContactSubFragmentViewModelFactory;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.ContactModelDiffCallback;
import vng.zalo.tdtai.zalo.zalo.viewmodels.ContactSubFragmentViewModel;
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_AVATAR;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_ID;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.ROOM_NAME;

public class ContactSubFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = ContactSubFragment.class.getSimpleName();
    private ContactSubFragmentViewModel viewModel;
    private RecyclerView recyclerView;
    private AllContactAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sub_fragment_contact,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, new ContactSubFragmentViewModelFactory()).get(ContactSubFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.allContactRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new AllContactAdapter(this, new ContactModelDiffCallback());

        recyclerView.setAdapter(adapter);

        viewModel.liveContacts.observe(getViewLifecycleOwner(), new Observer<List<RoomItem>>() {
            @Override
            public void onChanged(List<RoomItem> contacts) {
                adapter.submitList(contacts);
                Log.d(TAG,"onChanged livedata");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.itemContactSubFragmentConstraintLayout:
                int position = recyclerView.getChildLayoutPosition(v);
                Intent intent = new Intent(getActivity(), RoomActivity.class);

                intent.putExtra(ROOM_ID, adapter.getCurrentList().get(position).roomId);
                intent.putExtra(ROOM_NAME, adapter.getCurrentList().get(position).name);
                intent.putExtra(ROOM_AVATAR, adapter.getCurrentList().get(position).avatar);

                startActivity(intent);
        }
    }
}
