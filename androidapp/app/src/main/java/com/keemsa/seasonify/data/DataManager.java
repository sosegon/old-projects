package com.keemsa.seasonify.data;

import com.keemsa.seasonify.data.local.PreferencesHelper;
import com.keemsa.seasonify.data.remote.FirebaseHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class DataManager {

    private final PreferencesHelper mPreferencesHelper;
    private final FirebaseHelper mFirebaseHelper;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper) {
        mPreferencesHelper = preferencesHelper;
        mFirebaseHelper = new FirebaseHelper(); // Does not depend on anything
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public FirebaseHelper getFirebaseHelper() {
        return mFirebaseHelper;
    }
}
