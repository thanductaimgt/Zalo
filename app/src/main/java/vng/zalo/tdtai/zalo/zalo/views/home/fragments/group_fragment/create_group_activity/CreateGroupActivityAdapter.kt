package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.contacts_sub_fragment.ContactSubFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.official_account_sub_fragment.OfficialAccountSubFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.all_contacts_sub_fragment.AllContactsSubFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.recent_contacts_sub_fragment.RecentContactsSubFragment

class CreateGroupActivityAdapter(fm: FragmentManager):FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
    private var recentContactsFragment:Fragment? = null
    private var allContactsFragment:Fragment? = null

    override fun getItem(position: Int): Fragment {
        return when(position){
            1 -> {
                if(recentContactsFragment == null) {
                    recentContactsFragment = RecentContactsSubFragment()
                }
                recentContactsFragment!!
            }
            else -> {
                if(allContactsFragment == null) {
                    allContactsFragment = AllContactsSubFragment()
                }
                allContactsFragment!!
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

}