package com.keemsa.seasonify.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.keemsa.seasonify.injection.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class PreferencesHelper {

    public static final String PREF_FILE_NAME = "seasonify_pref_file";

    private final SharedPreferences mPref;

    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

}
