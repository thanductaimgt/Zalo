package vng.zalo.tdtai.zalo.ui.create_group

import android.view.View
import dagger.Binds
import dagger.Module

@Module
interface CreateGroupModule {
    @Binds
    fun bindClickListener(createGroupActivity: CreateGroupActivity): View.OnClickListener
}