package com.keemsa.seasonify.data;

import com.keemsa.seasonify.data.local.PreferencesHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class DataManager {

    private final PreferencesHelper mPreferencesHelper;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper) {
        mPreferencesHelper = preferencesHelper;
    }

    public PreferencesHelper getmPreferencesHelper() {
        return mPreferencesHelper;
    }
}
