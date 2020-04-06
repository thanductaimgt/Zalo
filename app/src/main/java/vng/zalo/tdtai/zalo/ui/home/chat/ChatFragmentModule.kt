package vng.zalo.tdtai.zalo.ui.home.chat

import android.view.View
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey

@Module
interface ChatFragmentModule {
    @Binds
    fun bindOnClickListener(chatFragment: ChatFragment): View.OnClickListener

    @Binds
    @IntoMap
    @ViewModelKey(ChatFragmentViewModel::class)
    fun bindRoomItemsViewModel(viewModel: ChatFragmentViewModel): ViewModel
}