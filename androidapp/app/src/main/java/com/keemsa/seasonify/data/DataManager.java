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

    public Observable<Bitmap> loadImage(final String path, final int imageWidth, final int imageHeight) {
        return Observable.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                InputStream input = new FileInputStream(path);
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, bounds);

                int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                Matrix matrix = new Matrix();
                matrix.postScale(imageWidth / width, imageHeight /height);
                Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);

                input.close();
                return scaledBitmap;
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Bitmap> classifyImage(final String path, final int imageSize) {

        return Observable.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {

                InputStream input = new FileInputStream(path);
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, bounds);

                int rotationAngle = getCameraPhotoOrientation(path);
                Timber.e("Rotate angle: " + rotationAngle);
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

                // passed to the classifier
                // TODO: set variable instead of 128
                Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, imageSize, imageSize, matrix, false);

                // saved to storage
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bounds.outWidth, bounds.outHeight, matrix, false);
                SeasonifyImage.saveImage(rotatedBitmap, path);

                input.close();

                return scaledBitmap;
            }

            private int getCameraPhotoOrientation(String path) {
                int rotate = 0;
                try {

                    ExifInterface exif = new ExifInterface(path);
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_NORMAL:
                            rotate = 0;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return rotate;
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

    }
}
