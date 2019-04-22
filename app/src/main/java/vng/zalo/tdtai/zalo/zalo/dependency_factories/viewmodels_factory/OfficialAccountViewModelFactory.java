package vng.zalo.tdtai.zalo.zalo.dependency_factories.viewmodels_factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import vng.zalo.tdtai.zalo.zalo.viewmodels.ContactSubFragmentViewModel;
import vng.zalo.tdtai.zalo.zalo.viewmodels.OfficialAccountViewModel;

public class OfficialAccountViewModelFactory implements ViewModelProvider.Factory {
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new OfficialAccountViewModel();
    }
}
