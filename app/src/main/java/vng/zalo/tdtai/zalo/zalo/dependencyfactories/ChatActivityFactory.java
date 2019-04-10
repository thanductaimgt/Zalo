package vng.zalo.tdtai.zalo.zalo.dependencyfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatActivityViewModel;

public class ChatActivityFactory implements ViewModelProvider.Factory {
    private Application application;

    public ChatActivityFactory(Application application){
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ChatActivityViewModel(application);
    }
}
