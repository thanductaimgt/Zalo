package vng.zalo.tdtai.zalo.zalo.factories.chat_activity

import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import vng.zalo.tdtai.zalo.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.zalo.views.activities.RoomActivity


@Module
class RoomActivityModule(private val roomActivity: RoomActivity) {

    @Provides
    internal fun providesViewModel(): RoomActivityViewModel {
        return ViewModelProviders.of(roomActivity, ViewModelFactory.getInstance(intent = roomActivity.intent))
                .get(RoomActivityViewModel::class.java)
    }
}