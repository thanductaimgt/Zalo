package vng.zalo.tdtai.zalo.ui.home.contacts

import dagger.BindsInstance
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface OfficialAccountComponent:AndroidInjector<OfficialAccountSubFragment> {
    @Subcomponent.Factory
    interface Factory{
        fun create(@BindsInstance fragment: OfficialAccountSubFragment):OfficialAccountComponent
    }
}