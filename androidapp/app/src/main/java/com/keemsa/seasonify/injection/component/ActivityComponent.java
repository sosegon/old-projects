package com.keemsa.seasonify.injection.component;

import com.keemsa.seasonify.features.start.MainActivity;
import com.keemsa.seasonify.injection.PerActivity;
import com.keemsa.seasonify.injection.module.ActivityModule;

import dagger.Subcomponent;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);

}
