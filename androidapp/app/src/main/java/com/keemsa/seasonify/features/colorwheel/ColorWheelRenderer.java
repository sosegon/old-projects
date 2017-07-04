package com.keemsa.seasonify.features.colorwheel;

import java.util.List;

/**
 * Created by sebastian on 04/07/17.
 */

public interface ColorWheelRenderer {
    float GAP_PERCENTAGE = 0.025f;

    void draw();

    ColorWheelRenderOption getRenderOption();

    void initWith(ColorWheelRenderOption colorWheelRenderOption);

    List<ColorCircle> getColorCircleList();
}
