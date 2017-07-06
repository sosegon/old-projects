package com.keemsa.seasonify.features.colorwheel;

import java.util.List;

/**
 * Created by sebastian on 04/07/17.
 */

public interface ColorWheelRenderer {

    void draw();

    ColorWheelRenderOption getRenderOption();

    void initWith(ColorWheelRenderOption colorWheelRenderOption);

    List<ColorElement> getColorElementList();

    void updateColorList(int[] colors);
}
