package com.keemsa.boilerplate;

import android.app.Application;
import android.content.Context;

import com.keemsa.boilerplate.injection.component.ApplicationComponent;
import com.keemsa.boilerplate.injection.component.DaggerApplicationComponent;
import com.keemsa.boilerplate.injection.module.ApplicationModule;

public class BoilerplateApplication extends Application {
    ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static BoilerplateApplication get(Context context) {
        return (BoilerplateApplication) context.getApplicationContext();
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
