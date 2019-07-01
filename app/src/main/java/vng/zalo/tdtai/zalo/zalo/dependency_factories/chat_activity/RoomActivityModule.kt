package vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity

import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import vng.zalo.tdtai.zalo.zalo.dependency_factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity


@Module
class RoomActivityModule(private val chatActivity: RoomActivity) {

    @Provides
    internal fun providesViewModel(): RoomActivityViewModel {
        return ViewModelProviders.of(chatActivity, ViewModelFactory(chatActivity.intent))
                .get(RoomActivityViewModel::class.java)
    }
}