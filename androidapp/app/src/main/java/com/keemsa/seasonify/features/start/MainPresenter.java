package com.keemsa.seasonify.features.start;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.keemsa.seasonify.base.BasePresenter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    public MainPresenter() {
    }

    public void classifyImage(Context context, File photoFile, Uri photoUri) {
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
    public void classify(Bitmap bitmap) {
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

        if(isViewAttached()) {
            getMvpView().updateFaceView();
            getMvpView().updateResult(results.get(0).getTitle());
        }
    }

}
