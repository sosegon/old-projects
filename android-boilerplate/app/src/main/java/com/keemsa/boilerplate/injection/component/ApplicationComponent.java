package com.keemsa.boilerplate.injection.component;

import android.app.Application;
import android.content.Context;

import com.keemsa.boilerplate.BoilerplateApplication;
import com.keemsa.boilerplate.data.DataManager;
import com.keemsa.boilerplate.data.local.PreferencesHelper;
import com.keemsa.boilerplate.injection.ApplicationContext;
import com.keemsa.boilerplate.injection.module.ApplicationModule;
import com.keemsa.boilerplate.util.RxEventBus;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(BoilerplateApplication boilerplateApplication);

    @ApplicationContext
    Context context();

    Application application();

    PreferencesHelper preferencesHelper();

    DataManager dataManager();

    RxEventBus eventBus();
}
