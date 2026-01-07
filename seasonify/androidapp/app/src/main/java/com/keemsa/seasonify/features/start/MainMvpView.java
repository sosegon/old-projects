package com.keemsa.seasonify.features.start;

import android.graphics.Bitmap;
import android.net.Uri;

import com.keemsa.colorwheel.ColorPickerView;
import com.keemsa.seasonify.base.MvpView;

import java.io.File;

/**
 * Created by sebastian on 3/27/17.
 */

public interface MainMvpView extends MvpView {

    void updatePrediction(String prediction, Bitmap bitmap, boolean isNewPrediction);
    void showToastMessage(String message);
    void endProcessing();
}
