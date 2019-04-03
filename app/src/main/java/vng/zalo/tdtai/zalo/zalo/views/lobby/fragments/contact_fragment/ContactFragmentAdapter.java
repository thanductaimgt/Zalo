package vng.zalo.tdtai.zalo.zalo.views.lobby.fragments.contact_fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import vng.zalo.tdtai.zalo.zalo.views.lobby.fragments.contact_fragment.tab_fragments.ContactSubFragment;
import vng.zalo.tdtai.zalo.zalo.views.lobby.fragments.contact_fragment.tab_fragments.OfficialAccountSubFragment;

public class ContactFragmentAdapter extends FragmentStatePagerAdapter {
    private Fragment officialAccountSubFragment;
    private Fragment contactSubFragment;

    ContactFragmentAdapter(FragmentManager fm) {
        super(fm);

        contactSubFragment = new ContactSubFragment();
        officialAccountSubFragment = new OfficialAccountSubFragment();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position){
            case 1:
                return officialAccountSubFragment;
            default:
                return contactSubFragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}