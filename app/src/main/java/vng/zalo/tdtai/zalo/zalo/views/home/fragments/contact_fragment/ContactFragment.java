package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import vng.zalo.tdtai.zalo.R;

public class ContactFragment extends Fragment implements TabLayout.OnTabSelectedListener {
    private ViewPager subContactViewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        subContactViewPager = view.findViewById(R.id.subContactViewPager);
        FragmentStatePagerAdapter contactFragmentAdapter = new ContactFragmentAdapter(getChildFragmentManager());
        subContactViewPager.setAdapter(contactFragmentAdapter);

        TabLayout tabLayout = view.findViewById(R.id.subContactTabLayout);
        subContactViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        subContactViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}