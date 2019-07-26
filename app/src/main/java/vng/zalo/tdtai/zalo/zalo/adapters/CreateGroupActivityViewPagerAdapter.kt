package vng.zalo.tdtai.zalo.zalo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vng.zalo.tdtai.zalo.zalo.views.fragments.AllContactsSubFragment
import vng.zalo.tdtai.zalo.zalo.views.fragments.RecentContactsSubFragment

class CreateGroupActivityViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> AllContactsSubFragment()
            else -> RecentContactsSubFragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}