package vng.zalo.tdtai.zalo.ui.create_group

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vng.zalo.tdtai.zalo.R
import javax.inject.Inject

class CreateGroupViewPagerAdapter @Inject constructor(
        private val activity: CreateGroupActivity
) : FragmentPagerAdapter(activity.supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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