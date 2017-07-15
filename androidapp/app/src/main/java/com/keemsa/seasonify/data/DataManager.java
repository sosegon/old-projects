package com.keemsa.seasonify.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.keemsa.seasonify.data.local.PreferencesHelper;
import com.keemsa.seasonify.data.remote.FirebaseHelper;
import com.keemsa.seasonify.util.SeasonifyImage;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


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

    public Observable<Bitmap> loadImage(String path, int imageWidth, int imageHeight) {
        return Observable.fromCallable(() -> SeasonifyImage.retrieveFromFile(path, imageWidth, imageHeight))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Bitmap> classifyImage(String path, int imageSize) {

        return Observable.fromCallable(() -> SeasonifyImage.retrieveFromFileToClassify(path, imageSize))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

    }
}
