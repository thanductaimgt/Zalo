package vng.zalo.tdtai.zalo.zalo.dependency_factories.application;

import android.app.Application;
import dagger.Component;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;

@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    void injectInto(ZaloApplication zaloApplication);
}