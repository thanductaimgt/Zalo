package vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity;

import androidx.lifecycle.ViewModelProviders;
import dagger.Module;
import dagger.Provides;
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel;
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.chat_activity.RoomActivity;


@Module
public class RoomActivityModule {
    private RoomActivity chatActivity;

    public RoomActivityModule(RoomActivity chatActivity){
        this.chatActivity = chatActivity;
    }

    @Provides
    RoomActivityViewModel providesViewModel(){
        return ViewModelProviders.of(chatActivity, new RoomActivityViewModelFactory(chatActivity.getIntent()))
                .get(RoomActivityViewModel.class);
    }
}