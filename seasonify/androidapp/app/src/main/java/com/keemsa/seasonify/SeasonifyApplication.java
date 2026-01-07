package com.keemsa.seasonify;

import android.app.Application;
import android.content.Context;

import com.keemsa.seasonify.injection.component.ApplicationComponent;
import com.keemsa.seasonify.injection.component.DaggerApplicationComponent;
import com.keemsa.seasonify.injection.module.ApplicationModule;

/**
 * Created by sebastian on 14/07/17.
 */

public class SeasonifyApplication extends Application {
    ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static SeasonifyApplication get(Context context) {
        return (SeasonifyApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        if (mApplicationComponent == null) {
            mApplicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return mApplicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }
}
