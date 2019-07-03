package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.contacts_sub_fragment.ContactSubFragment
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.official_account_sub_fragment.OfficialAccountSubFragment

class ContactFragmentAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var officialAccountSubFragment: Fragment? = null
    private var contactSubFragment: Fragment? = null

    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> {
                if(officialAccountSubFragment == null) {
                    officialAccountSubFragment = OfficialAccountSubFragment()
                }
                officialAccountSubFragment!!
            }
            else -> {
                if(contactSubFragment == null) {
                    contactSubFragment = ContactSubFragment()
                }
                contactSubFragment!!
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }
}