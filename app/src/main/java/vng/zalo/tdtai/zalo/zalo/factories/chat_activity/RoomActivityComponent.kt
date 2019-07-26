package vng.zalo.tdtai.zalo.zalo.factories.chat_activity

import dagger.Component
import vng.zalo.tdtai.zalo.zalo.views.activities.RoomActivity

@ActivityScope
@Component(modules = [RoomActivityModule::class])
interface RoomActivityComponent {
    fun inject(chatActivity: RoomActivity)
}