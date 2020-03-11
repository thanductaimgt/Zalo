package vng.zalo.tdtai.zalo.ui.call

import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.managers.CallService

@Module(includes = [CallModule.ProvideModule::class])
interface CallModule {
    @Binds
    @IntoMap
    @ViewModelKey(CallViewModel::class)
    fun bindCallViewModel(callViewModel: CallViewModel): ViewModel

    @Module
    class ProvideModule{
        @Provides
        fun intent(activity: CallActivity):Intent{
            return activity.intent
        }

        @Provides
        fun audioCallListener(activity: CallActivity):CallService.AudioCallListener{
            return activity.audioCallListener
        }
    }
}