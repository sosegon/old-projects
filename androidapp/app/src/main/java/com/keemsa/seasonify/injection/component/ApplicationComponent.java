package com.keemsa.seasonify.injection.component;

import android.app.Application;
import android.content.Context;

import com.keemsa.seasonify.SeasonifyApplication;
import com.keemsa.seasonify.data.DataManager;
import com.keemsa.seasonify.data.local.PreferencesHelper;
import com.keemsa.seasonify.processing.ImageClassifierHelper;
import com.keemsa.seasonify.processing.ImageProcessingHelper;
import com.keemsa.seasonify.injection.ApplicationContext;
import com.keemsa.seasonify.injection.module.ApplicationModule;
import com.keemsa.seasonify.util.RxEventBus;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(SeasonifyApplication seasonifyApplication);

    @ApplicationContext
    Context context();

    Application application();

    PreferencesHelper preferencesHelper();

    DataManager dataManager();

    ImageClassifierHelper imageClassifierHelper();

    ImageProcessingHelper imageProcessingHelper();

    RxEventBus eventBus();
}
