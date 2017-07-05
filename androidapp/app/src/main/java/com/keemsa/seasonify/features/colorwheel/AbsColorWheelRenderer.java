package com.keemsa.seasonify.features.colorwheel;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sebastian on 04/07/17.
 */

public abstract class AbsColorWheelRenderer implements ColorWheelRenderer {
    protected ColorWheelRenderOption colorWheelRenderOption;
    protected List<ColorCircle> colorCircleList = new ArrayList<>();
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


        Collections.sort(colorList, new Comparator<float[]>() {
            @Override
            public int compare(float[] lhs, float[] rhs) {
                return lhs[0] >= rhs[0] ? 1 : -1;
            }
        });
    }

    public void initWith(ColorWheelRenderOption colorWheelRenderOption) {
        this.colorWheelRenderOption = colorWheelRenderOption;
        this.colorCircleList.clear();
    }

    @Override
    public ColorWheelRenderOption getRenderOption() {
        if (colorWheelRenderOption == null) colorWheelRenderOption = new ColorWheelRenderOption();
        return colorWheelRenderOption;
    }

    public List<ColorCircle> getColorCircleList() {
        return colorCircleList;
    }

    protected int getAlphaValueAsInt() {
        return Math.round(colorWheelRenderOption.alpha * 255);
    }

    protected int calcTotalCount(float radius, float size) {
        return Math.max(1, (int) ((1f - GAP_PERCENTAGE) * Math.PI / (Math.asin(size / radius)) + 0.5f));
    }
}
