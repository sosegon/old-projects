package com.keemsa.seasonify.features.start;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;

import com.keemsa.seasonify.R;
import com.keemsa.seasonify.base.BasePresenter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindArray;
import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Created by sebastian on 3/27/17.
 */

public class MainPresenter extends BasePresenter<MainMvpView> implements  BitmapLoaderAsyncTask.BitmapLoaderAsyncTaskReceiver{

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private static final int INPUT_SIZE = 128;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input_images_input";
    private static final String OUTPUT_NAME = "output_labels/Softmax";

    private static final String MODEL_FILE = "file:///android_asset/seasonify.pb";
    private static final String LABEL_FILE = "file:///android_asset/seasonify.txt";

    @BindString(R.string.prf_prev_photo)
    String mPrfPrevPhotoKey;

    @BindString(R.string.prf_prev_prediction)
    String mPrfPrevPredictionKey;

    @BindArray(R.array.autumn_colors)
    int[] autumn_colors;

    @BindArray(R.array.spring_colors)
    int[] spring_colors;

    @BindArray(R.array.summer_colors)
    int[] summer_colors;

    @BindArray(R.array.winter_colors)
    int[] winter_colors;


    public MainPresenter(Activity activity) {
        ButterKnife.bind(this, activity);
    }

    public void classifyImage(Context context, File photoFile) {
        BitmapLoaderAsyncTask task = new BitmapLoaderAsyncTask(context, this, INPUT_SIZE);
        task.execute(photoFile.getAbsolutePath());
    }

    public File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    public void initTensorFlowAndLoadModel(final ContextWrapper contextWrapper) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            contextWrapper.getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    @Override
    public void classify(Context context, String path, Bitmap bitmap) {
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

        if(isViewAttached()) {

            String season = results.get(0).getTitle();
            getMvpView().updateFaceView(generateUri(context, new File(path)));
            getMvpView().updateResult(season);
            getMvpView().updatePalette(getSeasonalColors(season));
            storeResults(context, path, season);
        }
    }

    public Uri generateUri(Context context, File file) {
       return FileProvider.getUriForFile(
                context,
                "com.keemsa.seasonify.fileprovider",
                file
       );
    }

    public void loadPreviousResults(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String path = preferences.getString(mPrfPrevPhotoKey, "");
        String result = preferences.getString(mPrfPrevPredictionKey, "");

        if(isViewAttached()){
            if(!path.equals("") || !result.equals("")){
                getMvpView().updateFaceView(generateUri(context, new File(path)));
                getMvpView().updateResult(result);
                getMvpView().updatePalette(getSeasonalColors(result));
            }
        }
    }

    private void storeResults(Context context, String path, String prediction) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(mPrfPrevPhotoKey, path);
        editor.putString(mPrfPrevPredictionKey, prediction);
        editor.apply();
    }

    private int[] getSeasonalColors(String season) {
        if (season.equals("autumn")) {
            return autumn_colors;
        } else if (season.equals("spring")) {
            return spring_colors;
        } else if (season.equals("summer")) {
            return summer_colors;
        } else if (season.equals("winter")) {
            return winter_colors;
        }

        return new int[]{};
    }

}
