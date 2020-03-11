package vng.zalo.tdtai.zalo.ui.home.contacts

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ui.home.contacts.contact_sub.ContactSubFragment
import vng.zalo.tdtai.zalo.ui.home.contacts.official_account.OfficialAccountFragment
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Inject
import javax.inject.Named

class ContactFragmentAdapter @Inject constructor(
        private val context:Context,
        @Named(Constants.FRAGMENT_NAME) fm: FragmentManager
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> OfficialAccountFragment()
            else -> ContactSubFragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            1->context.getString(R.string.label_official_account_sub_tab)
            else->context.getString(R.string.label_contacts_tab)
        }
    }
}