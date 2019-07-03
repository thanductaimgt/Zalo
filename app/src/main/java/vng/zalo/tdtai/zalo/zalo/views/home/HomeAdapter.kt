package vng.zalo.tdtai.zalo.zalo.views.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.ChatFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.ContactFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.DiaryFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.GroupFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.MoreFragment

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