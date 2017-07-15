package com.keemsa.seasonify.features.start;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.ColorInt;

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
import com.keemsa.seasonify.processing.Classifier;
import com.keemsa.seasonify.processing.ImageClassifierHelper;
import com.keemsa.seasonify.processing.ImageProcessingHelper;
import com.keemsa.seasonify.util.SeasonifyImage;
import com.keemsa.seasonify.util.SeasonifyUtils;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static com.keemsa.seasonify.processing.ImageClassifierHelper.INPUT_SIZE;

/**
 * Created by sebastian on 3/27/17.
 */

@ConfigPersistent
public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;
    private final ImageClassifierHelper mImageClassifierHelper;
    private final ImageProcessingHelper mImageProcesssingHelper;

    public static final String ACTION_DATA_UPDATED = "com.keemsa.seasonify.ACTION_DATA_UPDATED";

    @Inject
    public MainPresenter(DataManager dataManager, ImageClassifierHelper imageClassifierHelper, ImageProcessingHelper imageProcessingHelper) {
        mDataManager = dataManager;
        mImageClassifierHelper = imageClassifierHelper;
        mImageProcesssingHelper = imageProcessingHelper;
    }

    public boolean hasStoredPrediction() {
        String predictedSeason = mDataManager.getPreferencesHelper().retrievePrediction();
        String photoPath = mDataManager.getPreferencesHelper().retrievePhotoPath();

        return !(predictedSeason.equals("") && photoPath.equals(""));
    }

    public void classifyImage(Context context, File photoFile) {

        String path = photoFile.getAbsolutePath();

        mDataManager.classifyImage(path, INPUT_SIZE)
                    .subscribe((y) -> classify(context, path));
    }

    public void loadSavedPhoto() {
        mDataManager.loadImage(
                        mDataManager.getPreferencesHelper().retrievePhotoPath(),
                        INPUT_SIZE,
                        INPUT_SIZE)
                    .subscribe((y) -> load(y));
    }

    public String getStoredPrediction() {
        return mDataManager.getPreferencesHelper().retrievePrediction();
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

    private void load(Bitmap bitmap) {
        String predictedSeason = getStoredPrediction();

        if (isViewAttached()) {
            if (bitmap != null && !predictedSeason.equals("")) {
                updateViewUponPrediction(predictedSeason, bitmap);
            }
        }
    }

    private void classify(final Context context, String path) {
        Bitmap faceBitmap = mImageProcesssingHelper.detectFace(path, INPUT_SIZE);

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
                Uri photoUri2 = Uri.fromFile(new File(faceOnlyPath)); // To store in firebase

                updateViewUponPrediction(season, faceBitmap);

                mDataManager.getPreferencesHelper().storePrediction(season);
                mDataManager.getPreferencesHelper().storePhotoPath(faceOnlyPath);

                int seasonInt = getPredictionAsInteger(season);
                StorageReference facePhotoRef = mDataManager.getFirebaseHelper().getFacePhotoReference(photoUri2.getLastPathSegment());
                facePhotoRef.putFile(photoUri2)
                        .addOnSuccessListener((y) -> onSuccessPutFile(context, y, seasonInt))
                        .addOnFailureListener((y) -> Timber.e("Error when storing file: " + y.getMessage()));
            }
        } else {
            getMvpView().showToastMessage(context.getString(R.string.msg_face_no_detected));
        }
    }

    private void onSuccessPutFile(Context context, UploadTask.TaskSnapshot taskSnapshot, int seasonInt) {
        try {
            Uri downloadUrl = taskSnapshot.getDownloadUrl();
            Prediction prediction = new Prediction(seasonInt, downloadUrl.toString());
            mDataManager.getFirebaseHelper().storePrediction(prediction);

            // Send broadcast to update the widgets
            Intent updateIntent = new Intent(ACTION_DATA_UPDATED);
            updateIntent.setPackage(context.getPackageName());
            context.sendBroadcast(updateIntent);
        } catch (NullPointerException e) {
            Timber.e(e.getMessage());
        }
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
