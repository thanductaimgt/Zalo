package vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel;

public class RoomActivityViewModelFactory implements ViewModelProvider.Factory {
    private Intent intent;

    RoomActivityViewModelFactory(Intent intent){
        this.intent = intent;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RoomActivityViewModel(intent);
    }
}
