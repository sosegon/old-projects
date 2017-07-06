package com.keemsa.seasonify.features.colorwheel;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastian on 04/07/17.
 */

public abstract class AbsColorWheelRenderer implements ColorWheelRenderer {
    protected ColorWheelRenderOption colorWheelRenderOption;
    protected List<ColorElement> colorElementList = new ArrayList<>();
    protected List<float[]> colorList = new ArrayList<>();

    @Override
    public void updateColorList(int[] colors) {
        colorList.clear();
        for(int i = 0; i < colors.length; i++) {
            int currentColor = colors[i];
            float[] hsv = new float[3];
            Color.colorToHSV(currentColor, hsv);
            colorList.add(hsv);
        }
    }

    public void initWith(ColorWheelRenderOption colorWheelRenderOption) {
        this.colorWheelRenderOption = colorWheelRenderOption;
        this.colorElementList.clear();
    }

    @Override
    public ColorWheelRenderOption getRenderOption() {
        if (colorWheelRenderOption == null) colorWheelRenderOption = new ColorWheelRenderOption();
        return colorWheelRenderOption;
    }

    public List<ColorElement> getColorElementList() {
        return colorElementList;
    }

}
