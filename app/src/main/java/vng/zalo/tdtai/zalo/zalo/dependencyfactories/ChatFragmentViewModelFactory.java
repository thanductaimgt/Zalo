package vng.zalo.tdtai.zalo.zalo.dependencyfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatFragmentViewModel;

public class ChatFragmentViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    public ChatFragmentViewModelFactory(Application application){
        this.application = application;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ChatFragmentViewModel(application);
    }
}
