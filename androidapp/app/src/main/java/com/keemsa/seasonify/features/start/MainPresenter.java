package com.keemsa.seasonify.features.start;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.keemsa.colorwheel.ColorElement;
import com.keemsa.colorwheel.ColorPickerView;
import com.keemsa.seasonify.BuildConfig;
import com.keemsa.seasonify.R;
import com.keemsa.seasonify.base.BasePresenter;
import com.keemsa.seasonify.data.DataManager;
import com.keemsa.seasonify.injection.ConfigPersistent;
import com.keemsa.seasonify.model.Prediction;
import com.keemsa.seasonify.util.Cluster;
import com.keemsa.seasonify.util.SeasonifyImage;
import com.keemsa.seasonify.util.SeasonifyUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by sebastian on 3/27/17.
 */

@ConfigPersistent
public class MainPresenter extends BasePresenter<MainMvpView> {

    private final String LOG_TAG = MainPresenter.class.getSimpleName();

    private final DataManager mDataManager;
    private final ImageClassifierHelper mImageClassifierHelper;

    public static final String ACTION_DATA_UPDATED = "com.keemsa.seasonify.ACTION_DATA_UPDATED";

    @Inject
    public MainPresenter(DataManager dataManager, ImageClassifierHelper imageClassifierHelper) {
        mDataManager = dataManager;
        mImageClassifierHelper = imageClassifierHelper;
    }

    public boolean hasStoredPrediction() {
        String predictedSeason = mDataManager.getPreferencesHelper().retrievePrediction();
        String photoPath = mDataManager.getPreferencesHelper().retrievePhotoPath();

        return !(predictedSeason.equals("") && photoPath.equals(""));
    }

    public void classifyImage(final Context context, File photoFile) {

        final String path = photoFile.getAbsolutePath();

        mDataManager.classifyImage(path, mImageClassifierHelper.INPUT_SIZE)
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                        classify(context, path, bitmap);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public File createImageFile(Context context) throws IOException {

        // The directory for the picture
        final String appName = context.getResources().getString(R.string.app_name);
        File storageDir = context.getExternalFilesDir(appName);

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    private void load(Bitmap bitmap) {
        String predictedSeason = getStoredPrediction();

        if (isViewAttached()) {
            if (bitmap != null && !predictedSeason.equals("")) {
                updateViewUponPrediction(predictedSeason, bitmap);
            }
        }
    }

    private void classify(final Context context, String path, Bitmap bitmap) {
        Bitmap faceBitmap = mImageClassifierHelper.detectFace(path);

        if (faceBitmap != null) {

            String faceOnlyPath = SeasonifyUtils.getFileNameNoExtension(path) + "_faceOnly.jpg";
            SeasonifyImage.saveImage(faceBitmap, faceOnlyPath);

            // TODO: Check this when releasing the app
            if (BuildConfig.DEBUG) {
                SeasonifyImage.addImageToGallery(context, faceOnlyPath);
            }

            final List<Classifier.Recognition> results = mImageClassifierHelper.classifyImage(faceBitmap);

            if (isViewAttached()) {

                String season = results.get(0).getTitle();
                Uri photoUri = generateUri(context, new File(path)); // To update the view
                Uri photoUri2 = Uri.fromFile(new File(faceOnlyPath)); // To store in firebase

                updateViewUponPrediction(season, faceBitmap);

                storePrediction(season);
                storePhotoPath(faceOnlyPath);

                final int seasonInt = getPredictionAsInteger(season);
                StorageReference facePhotoRef = mDataManager.getFirebaseHelper().getFacePhotoReference(photoUri2.getLastPathSegment());
                facePhotoRef.putFile(photoUri2).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Prediction prediction = new Prediction(seasonInt, downloadUrl.toString());
                        mDataManager.getFirebaseHelper().storePrediction(prediction);

                        // Send broadcast to update the widgets
                        Intent updateIntent = new Intent(ACTION_DATA_UPDATED);
                        updateIntent.setPackage(context.getPackageName());
                        context.sendBroadcast(updateIntent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Error when storing file: " + e.getMessage());
                    }
                });
            }
        } else {
            getMvpView().showToastMessage(context.getString(R.string.msg_face_no_detected));
        }

    }

    public Uri generateUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                "com.keemsa.seasonify.fileprovider",
                file
        );
    }

    public void loadSavedPhoto() {
        mDataManager.loadImage(getStoredPhotoPath(), mImageClassifierHelper.INPUT_SIZE, mImageClassifierHelper.INPUT_SIZE)
                    .subscribe(new Observer<Bitmap>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                            load(bitmap);
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
    }

    public String getStoredPhotoPath() {
        return mDataManager.getPreferencesHelper().retrievePhotoPath();
    }

    public void storePhotoPath(String path) {
        mDataManager.getPreferencesHelper().storePhotoPath(path);
    }

    public String getStoredPrediction() {
        return mDataManager.getPreferencesHelper().retrievePrediction();
    }

    public void storePrediction(String prediction) {
        mDataManager.getPreferencesHelper().storePrediction(prediction);
    }

    public int getStoredColorSelectionType() {
        return mDataManager.getPreferencesHelper().retrieveColorSelectionType();
    }

    public int storeColorSelectionType(ColorPickerView.COLOR_SELECTION colorSelection) {
        return mDataManager.getPreferencesHelper().storeColorSelectionType(colorSelection);
    }

    public float[] getStoredSelectedColorCoords() {
        return mDataManager.getPreferencesHelper().retrieveSelectedColorCoords();
    }

    public void storeSelectedColorCoords(List<ColorElement> colors) {
        mDataManager.getPreferencesHelper().storeSelectedColorCoords(colors);
    }

    public void storeColorCombination(@ColorInt int[] colors) {
        mDataManager.getPreferencesHelper().addColorCombination(colors);
    }

    public List<int[]> getStoredColorCombinations() {
        return mDataManager.getPreferencesHelper().retrieveColorCombinations();
    }

    public boolean existColorCombination(int[] colors) {
        return mDataManager.getPreferencesHelper().hasColorCombination(colors);
    }

    public boolean removeStoredColorCombination(int[] colors) {
        return mDataManager.getPreferencesHelper().deleteColorCombination(colors);
    }

    private int getPredictionAsInteger(String season) {
        if (season.equals("autumn")) {
            return 0;
        } else if (season.equals("spring")) {
            return 1;
        } else if (season.equals("summer")) {
            return 2;
        } else if (season.equals("winter")) {
            return 3;
        }
        return -1;
    }

    private void updateViewUponPrediction(String prediction, Bitmap bitmap) {
        getMvpView().updatePrediction(prediction);
        getMvpView().updateColorWheel(prediction, bitmap);
        getMvpView().updateColorSelection(getStoredColorSelectionType());
    }
}
