package com.keemsa.boilerplate.injection.module;

import android.app.Service;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sebastian on 14/07/17.
 */

@Module
public class ServiceModule {

    private Service mService;

    public ServiceModule(Service service) {
        mService = service;
    }

    @Provides
    Service provideService() {
        return mService;
    }

}
