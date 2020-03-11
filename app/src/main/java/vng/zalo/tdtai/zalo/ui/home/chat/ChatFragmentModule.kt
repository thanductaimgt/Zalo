package vng.zalo.tdtai.zalo.ui.home.chat

import android.view.View
import dagger.Binds
import dagger.Module

@Module
interface ChatFragmentModule {
    @Binds
    fun bindOnClickListener(chatFragment: ChatFragment): View.OnClickListener
}