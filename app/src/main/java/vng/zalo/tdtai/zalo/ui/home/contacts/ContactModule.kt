package vng.zalo.tdtai.zalo.ui.home.contacts

import androidx.fragment.app.FragmentManager
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import vng.zalo.tdtai.zalo.ui.home.contacts.contact_sub.ContactSubFragment
import vng.zalo.tdtai.zalo.ui.home.contacts.contact_sub.ContactSubFragmentModule
import vng.zalo.tdtai.zalo.ui.home.contacts.official_account.OfficialAccountModule
import vng.zalo.tdtai.zalo.ui.home.contacts.official_account.OfficialAccountFragment
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Named

@Module(includes = [ContactModule.ProvideModule::class])
interface ContactModule{
    @ContributesAndroidInjector(modules = [ContactSubFragmentModule::class])
    fun contactSubFragment():ContactSubFragment

    @ContributesAndroidInjector(modules = [OfficialAccountModule::class])
    fun officialAccountSubFragment():OfficialAccountFragment

    @Module
    class ProvideModule{
        @Provides
        @Named(Constants.FRAGMENT_NAME)
        fun fragmentManager(fragment: ContactFragment):FragmentManager{
            return fragment.childFragmentManager
        }
    }
}