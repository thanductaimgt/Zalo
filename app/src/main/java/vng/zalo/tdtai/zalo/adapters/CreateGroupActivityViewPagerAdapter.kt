package vng.zalo.tdtai.zalo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.views.activities.CreateGroupActivity
import vng.zalo.tdtai.zalo.views.fragments.AllContactsSubFragment
import vng.zalo.tdtai.zalo.views.fragments.RecentContactsSubFragment

class CreateGroupActivityViewPagerAdapter(private val activity:CreateGroupActivity, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> AllContactsSubFragment()
            else -> RecentContactsSubFragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            1->activity.getString(R.string.label_contacts_tab)
            else->activity.getString(R.string.label_recent)
        }
    }
}