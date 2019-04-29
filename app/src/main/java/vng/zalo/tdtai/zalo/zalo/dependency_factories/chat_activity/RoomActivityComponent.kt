package vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity

import dagger.Component
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity

@ActivityScope
@Component(modules = [RoomActivityModule::class])
interface RoomActivityComponent {
    fun inject(chatActivity: RoomActivity)
}