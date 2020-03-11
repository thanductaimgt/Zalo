package vng.zalo.tdtai.zalo.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.ui.home.chat.ChatFragment
import vng.zalo.tdtai.zalo.ui.home.contacts.ContactFragment
import vng.zalo.tdtai.zalo.ui.home.diary.DiaryFragment
import vng.zalo.tdtai.zalo.ui.home.group.GroupFragment
import vng.zalo.tdtai.zalo.ui.home.more.MoreFragment
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Inject
import javax.inject.Named

class HomeAdapter @Inject constructor(
        @Named(Constants.ACTIVITY_NAME) fm: FragmentManager
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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