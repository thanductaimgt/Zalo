package vng.zalo.tdtai.zalo.ui.home.contacts.contact_sub

import android.view.View
import dagger.Binds
import dagger.Module

@Module
interface ContactSubFragmentModule {
    @Binds
    fun bindClickListener(contactSubFragment: ContactSubFragment):View.OnClickListener
}