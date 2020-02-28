package vng.zalo.tdtai.zalo.ui.call.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.managers.CallService
import vng.zalo.tdtai.zalo.ui.call.CallActivity
import vng.zalo.tdtai.zalo.ui.call.CallViewModel

@Module
interface CallModule {
    @Binds
    fun bindCallListener(audioCallListener: CallActivity.AudioCallListener):CallService.AudioCallListener

    @Binds
    @IntoMap
    @ViewModelKey(CallViewModel::class)
    fun bindCallViewModel(callViewModel: CallViewModel): ViewModel
}