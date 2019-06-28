package vng.zalo.tdtai.zalo.zalo.views.home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;

public class MoreFragment extends Fragment {
    private ImageView avatarImgView;
    private TextView nameTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        avatarImgView = view.findViewById(R.id.avatarImgView);
        nameTextView = view.findViewById(R.id.nameTextView);

        Picasso.get()
                .load(ZaloApplication.currentUser.avatar)
                .fit()
                .into(avatarImgView);

        nameTextView.setText(ZaloApplication.currentUser.phone);
    }
}