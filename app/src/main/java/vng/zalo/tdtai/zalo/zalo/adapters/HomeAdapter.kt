package vng.zalo.tdtai.zalo.zalo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.zalo.views.fragments.ChatFragment
import vng.zalo.tdtai.zalo.zalo.views.fragments.ContactFragment
import vng.zalo.tdtai.zalo.zalo.views.fragments.DiaryFragment
import vng.zalo.tdtai.zalo.zalo.views.fragments.GroupFragment
import vng.zalo.tdtai.zalo.zalo.views.fragments.MoreFragment

class HomeAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> ContactFragment()
            2 -> GroupFragment()
            3 -> DiaryFragment()
            4 -> MoreFragment()
            else -> ChatFragment()
        }
    }

    override fun getCount(): Int {
        return 5
    }
}