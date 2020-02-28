package vng.zalo.tdtai.zalo.ui.create_group

import dagger.BindsInstance
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent(modules = [CreateGroupModule::class])
interface CreateGroupComponent:AndroidInjector<CreateGroupActivity>{
    @Subcomponent.Factory
    interface Factory{
        fun create(@BindsInstance activity: CreateGroupActivity):CreateGroupComponent
    }
}