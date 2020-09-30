package com.mgt.zalo.ui.create_group

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mgt.zalo.R
import javax.inject.Inject

class CreateGroupPagerAdapter @Inject constructor(
        private val createGroupActivity: CreateGroupActivity
) : FragmentStateAdapter(createGroupActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> AllContactsFragment()
            else -> RecentContactsFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    fun getTabTitle(position: Int): CharSequence? {
        return when (position) {
            1 -> createGroupActivity.getString(R.string.label_contacts_tab)
            else -> createGroupActivity.getString(R.string.label_recent)
        }
    }
}