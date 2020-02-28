package vng.zalo.tdtai.zalo.ui.call.di

import android.content.Intent
import dagger.BindsInstance
import dagger.Subcomponent
import dagger.android.AndroidInjector
import vng.zalo.tdtai.zalo.ui.call.CallActivity


@Subcomponent(modules = [CallModule::class])
interface CallComponent: AndroidInjector<CallActivity> {
    @Subcomponent.Factory
    interface Factory{
        fun create(
                @BindsInstance intent: Intent,
                @BindsInstance audioCallListener: CallActivity.AudioCallListener
        ): CallComponent
    }
}