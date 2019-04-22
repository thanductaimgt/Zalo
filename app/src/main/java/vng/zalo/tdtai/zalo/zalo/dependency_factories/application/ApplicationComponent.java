package vng.zalo.tdtai.zalo.zalo.dependency_factories.application;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    void inject(ZaloApplication zaloApplication);
}