package vng.zalo.tdtai.zalo.screens.lobby;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import vng.zalo.tdtai.zalo.screens.lobby.fragments.chat_fragment.ChatFragment;
import vng.zalo.tdtai.zalo.screens.lobby.fragments.ContactFragment;
import vng.zalo.tdtai.zalo.screens.lobby.fragments.DiaryFragment;
import vng.zalo.tdtai.zalo.screens.lobby.fragments.GroupFragment;
import vng.zalo.tdtai.zalo.screens.lobby.fragments.MoreFragment;

public class LobbyPagerAdapter extends FragmentStatePagerAdapter {
    private Fragment chatFragment;
    private Fragment contactFragment;
    private Fragment groupFragment;
    private Fragment diaryFragment;
    private Fragment moreFragment;

    LobbyPagerAdapter(FragmentManager fm) {
        super(fm);

        chatFragment = new ChatFragment();
        contactFragment = new ContactFragment();
        groupFragment = new GroupFragment();
        diaryFragment = new DiaryFragment();
        moreFragment = new MoreFragment();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position){
            case 1:
                return contactFragment;
            case 2:
                return groupFragment;
            case 3:
                return diaryFragment;
            case 4:
                return moreFragment;
            default:
                return chatFragment;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}