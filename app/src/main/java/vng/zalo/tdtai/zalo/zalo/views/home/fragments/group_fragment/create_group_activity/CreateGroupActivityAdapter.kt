package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.all_contacts_sub_fragment.AllContactsSubFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.recent_contacts_sub_fragment.RecentContactsSubFragment

class CreateGroupActivityAdapter(fm: FragmentManager):FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
    override fun getItem(position: Int): Fragment {
        return when(position){
            1 -> AllContactsSubFragment()
            else -> RecentContactsSubFragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}