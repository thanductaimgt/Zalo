package vng.zalo.tdtai.zalo.ui.home.contacts

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ui.home.contacts.ContactSubFragment
import vng.zalo.tdtai.zalo.ui.home.contacts.OfficialAccountSubFragment

class ContactFragmentAdapter(private val fragment:Fragment, fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var officialAccountSubFragment: Fragment? = null
    private var contactSubFragment: Fragment? = null

    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> {
                if (officialAccountSubFragment == null) {
                    officialAccountSubFragment = OfficialAccountSubFragment()
                }
                officialAccountSubFragment!!
            }
            else -> {
                if (contactSubFragment == null) {
                    contactSubFragment = ContactSubFragment()
                }
                contactSubFragment!!
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            1->fragment.getString(R.string.label_official_account_sub_tab)
            else->fragment.getString(R.string.label_contacts_tab)
        }
    }
}