package com.keemsa.boilerplate.injection.component;


import com.keemsa.boilerplate.features.list.MessagesFragment;
import com.keemsa.boilerplate.features.start.MainFragment;
import com.keemsa.boilerplate.injection.PerActivity;
import com.keemsa.boilerplate.injection.module.ActivityModule;

import dagger.Subcomponent;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {
    void inject(MainFragment mainFragment);
    void inject(MessagesFragment messagesFragment);
}
