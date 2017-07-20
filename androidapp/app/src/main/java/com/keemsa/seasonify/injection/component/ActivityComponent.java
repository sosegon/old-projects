package com.keemsa.seasonify.injection.component;

import com.keemsa.seasonify.features.combinations.CombinationsFragment;
import com.keemsa.seasonify.features.start.MainFragment;
import com.keemsa.seasonify.injection.PerActivity;
import com.keemsa.seasonify.injection.module.ActivityModule;

import dagger.Subcomponent;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainFragment mainFragment);

    void inject(CombinationsFragment combinationsFragment);

}
