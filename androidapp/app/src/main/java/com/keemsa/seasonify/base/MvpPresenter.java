package com.keemsa.seasonify.base;

/**
 * Created by sebastian on 3/27/17.
 * based on code from https://github.com/ribot/android-boilerplate
 */

public interface MvpPresenter<V extends MvpView> {
    void attachView(V mpvView);
    void detachView();
}
