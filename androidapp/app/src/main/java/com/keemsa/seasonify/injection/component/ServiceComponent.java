package com.keemsa.seasonify.injection.component;

import com.keemsa.seasonify.features.widget.SeasonWidgetIntentService;
import com.keemsa.seasonify.injection.PerService;
import com.keemsa.seasonify.injection.module.ServiceModule;

import dagger.Component;

/**
 * Created by sebastian on 14/07/17.
 */

@PerService
@Component(dependencies = ApplicationComponent.class, modules = ServiceModule.class)
public interface ServiceComponent {

    void inject(SeasonWidgetIntentService seasonWidgetIntentService);
}
