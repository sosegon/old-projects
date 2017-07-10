package com.keemsa.colorwheel;

import android.graphics.Bitmap;
import android.graphics.Canvas;

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

    List<ColorElement> getColorElements(ColorPickerView.COLOR_SELECTION colorSelection, float x, float y);

    void drawSelected(Canvas canvas, List<ColorElement> colors);
}
