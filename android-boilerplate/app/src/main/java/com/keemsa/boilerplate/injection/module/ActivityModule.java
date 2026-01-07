package com.keemsa.boilerplate.injection.module;

import android.app.Activity;
import android.content.Context;


import com.keemsa.boilerplate.injection.ActivityContext;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sebastian on 14/07/17.
 */

@Module
public class ActivityModule {

    private Activity mActivity;

    public ActivityModule(Activity activity) {
        mActivity = activity;
    }

    @Provides
    Activity provideActivity() {
        return mActivity;
    }

    @Provides
    @ActivityContext
    Context providesContext() {
        return mActivity;
    }
}
