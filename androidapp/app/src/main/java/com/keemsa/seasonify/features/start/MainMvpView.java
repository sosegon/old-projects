package com.keemsa.seasonify.features.start;

import android.net.Uri;

import com.keemsa.seasonify.base.MvpView;

/**
 * Created by sebastian on 3/27/17.
 */

public interface MainMvpView extends MvpView {

    void updateFaceView(Uri uriPhoto);
    void updateResult(String result);
}
