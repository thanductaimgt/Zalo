package vng.zalo.tdtai.zalo.ui.home.group

import android.view.View
import dagger.Binds
import dagger.Module

@Module
interface GroupFragmentModule {
    @Binds
    fun bindClickListener(groupFragment: GroupFragment): View.OnClickListener
}