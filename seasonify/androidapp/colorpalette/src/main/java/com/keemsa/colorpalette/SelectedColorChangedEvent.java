package com.keemsa.colorpalette;

import android.support.annotation.ColorInt;

/**
 * Created by sebastian on 10/07/17.
 */

public class SelectedColorChangedEvent {
    private @ColorInt int mSelectedColor;

    public SelectedColorChangedEvent(@ColorInt int color) {
        mSelectedColor = color;
    }

    public @ColorInt
    int getSelectedColor() {
        return mSelectedColor;
    }
}
