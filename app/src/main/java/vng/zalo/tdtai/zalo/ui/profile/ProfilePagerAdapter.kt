package vng.zalo.tdtai.zalo.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import vng.zalo.tdtai.zalo.ui.profile.diary.ProfileDiaryFragment
import vng.zalo.tdtai.zalo.ui.profile.media.ProfileMediaFragment
import javax.inject.Inject

class ProfilePagerAdapter @Inject constructor(
        profileFragment: ProfileFragment
) : FragmentStateAdapter(profileFragment) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1-> ProfileMediaFragment()
            else -> ProfileDiaryFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}