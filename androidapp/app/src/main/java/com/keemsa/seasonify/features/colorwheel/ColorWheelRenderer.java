package com.keemsa.seasonify.features.colorwheel;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by sebastian on 04/07/17.
 */

public interface ColorWheelRenderer {

    void drawColor();

    ColorWheelRenderOption getRenderOption();

    void initWith(ColorWheelRenderOption colorWheelRenderOption);

    List<ColorElement> getColorElementList();

    void updateColorList(int[] colors);

    void drawCenter();

    void setCenter(Bitmap bitmap);
}
