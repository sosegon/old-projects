package com.keemsa.seasonify.processing;

import android.content.Context;
import android.graphics.Bitmap;

import com.keemsa.seasonify.injection.ApplicationContext;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class ImageClassifierHelper {

    private static final String LOG_TAG = ImageClassifierHelper.class.getSimpleName();

    private final Context mContext;
    private Classifier mClassifier;

    public static final int INPUT_SIZE = 128;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input_images_input";
    private static final String OUTPUT_NAME = "output_labels/Softmax";

    private static final String MODEL_FILE = "file:///android_asset/seasonify.pb";
    private static final String LABEL_FILE = "file:///android_asset/seasonify.txt";

    @Inject
    public ImageClassifierHelper(@ApplicationContext Context context) {
        mContext = context;
        initTensorFlowAndLoadModel();
    }

    public List<Classifier.Recognition> classifyImage(Bitmap bitmap) {
        return mClassifier.recognizeImage(bitmap);
    }

    private void initTensorFlowAndLoadModel() {
        try {
            mClassifier = TensorFlowImageClassifier.create(
                    mContext.getAssets(),
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
}
