package com.keemsa.boilerplate.data;

import com.keemsa.boilerplate.data.local.PreferencesHelper;
import com.keemsa.boilerplate.data.remote.FirebaseHelper;
import com.keemsa.boilerplate.util.RxEventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataManager {

    public static final String ACTION_DATA_UPDATED = "com.keemsa.boilerplate.ACTION_DATA_UPDATED";

    private final PreferencesHelper mPreferencesHelper;
    private final FirebaseHelper mFirebaseHelper;
    private final RxEventBus mEventBus;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper, RxEventBus eventBus) {
        mPreferencesHelper = preferencesHelper;
        mFirebaseHelper = new FirebaseHelper(); // Does not depend on anything
        mEventBus = eventBus;
        initBus();
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public FirebaseHelper getFirebaseHelper() {
        return mFirebaseHelper;
    }

    private void initBus() {

    }
}
