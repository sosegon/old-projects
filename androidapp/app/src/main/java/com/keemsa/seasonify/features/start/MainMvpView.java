package com.keemsa.seasonify.features.start;

import android.graphics.Bitmap;
import android.net.Uri;

import com.keemsa.seasonify.base.MvpView;

import java.io.File;

/**
 * Created by sebastian on 3/27/17.
 */

public interface MainMvpView extends MvpView {

    void updateResult(String result);
    void updateColorWheel(int colors[], Bitmap bitmap);
}
