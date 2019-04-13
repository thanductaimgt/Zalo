package vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatActivityViewModel;

public class ChatActivityViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private Intent intent;

    public ChatActivityViewModelFactory(Intent intent, Application application){
        this.application = application;
        this.intent = intent;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ChatActivityViewModel(intent, application);
    }
}
