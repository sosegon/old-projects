package com.keemsa.seasonify.features.start;

import android.content.Context;
import android.graphics.Bitmap;

import com.keemsa.seasonify.BuildConfig;
import com.keemsa.seasonify.R;
import com.keemsa.seasonify.base.BasePresenter;
import com.keemsa.seasonify.data.DataManager;
import com.keemsa.seasonify.injection.ConfigPersistent;
import com.keemsa.seasonify.processing.Classifier;
import com.keemsa.seasonify.processing.ImageClassifierHelper;
import com.keemsa.seasonify.processing.ImageProcessingHelper;
import com.keemsa.seasonify.util.SeasonifyImage;
import com.keemsa.seasonify.util.SeasonifyUtils;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import static com.keemsa.seasonify.processing.ImageClassifierHelper.INPUT_SIZE;

/**
 * Created by sebastian on 3/27/17.
 */

@ConfigPersistent
public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;
    private final ImageClassifierHelper mImageClassifierHelper;
    private final ImageProcessingHelper mImageProcesssingHelper;

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
        if(hasStoredPrediction()){
            mDataManager.loadImage(
                    mDataManager.getPreferencesHelper().retrievePhotoPath(),
                    INPUT_SIZE,
                    INPUT_SIZE)
                    .subscribe((y) -> load(y));
        }
    }

    public String getStoredPrediction() {
        return mDataManager.getPreferencesHelper().retrievePrediction();
    }

    public int getStoredColorSelectionType() {
        return mDataManager.getPreferencesHelper().retrieveColorSelectionType();
    }

    public float[] getStoredSelectedColorCoords() {
        return mDataManager.getPreferencesHelper().retrieveSelectedColorCoords();
    }

    public boolean existColorCombination(int[] colors) {
        return mDataManager.getPreferencesHelper().hasColorCombination(colors);
    }

    private void load(Bitmap bitmap) {
        if (isViewAttached() && bitmap != null) {
            getMvpView().updatePrediction(mDataManager.getPreferencesHelper().retrievePrediction(), bitmap, false);
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

                getMvpView().updatePrediction(season, faceBitmap, true);

                mDataManager.storePrediction(context, season, faceOnlyPath);
            }
        } else {
            getMvpView().showToastMessage(context.getString(R.string.msg_face_no_detected));
        }
        getMvpView().endProcessing();
    }
}
