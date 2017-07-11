package com.keemsa.colorwheel;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastian on 04/07/17.
 */

public abstract class AbsColorWheelRenderer implements ColorWheelRenderer {
    protected ColorWheelRenderOption colorWheelRenderOption;
    protected List<ColorElement> colorElementList = new ArrayList<>();
    protected List<float[]> colorList = new ArrayList<>();
    protected Bitmap center;
    protected float strokeWidth = 4f;

    @Override
    public void setCenter(Bitmap center) {
        this.center = center;
    }

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
    }

    @Override
    public ColorWheelRenderOption getRenderOption() {
        if (colorWheelRenderOption == null) colorWheelRenderOption = new ColorWheelRenderOption();
        return colorWheelRenderOption;
    }

    @Override
    public List<ColorElement> getColorElementList() {
        return colorElementList;
    }


    @Override
    public void drawCenter() {
        if(center == null)
            return;

        // Use a new paint to avoid blank bitmap due to setting
        // xfermode
        Paint selectorFill2 = PaintBuilder.newPaint().antiAlias(true).build();
        float half = colorWheelRenderOption.targetCanvas.getWidth() / 2f;
        float radius = colorWheelRenderOption.radius;
        float innerRadius = colorWheelRenderOption.centerRadiusRatio * radius;

        float x = half - innerRadius;
        float y = x;

        colorWheelRenderOption.targetCanvas.drawCircle(half, half, innerRadius, selectorFill2);
        selectorFill2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        RectF dst = new RectF(x, y, x + 2 * innerRadius, y + 2 * innerRadius);
        colorWheelRenderOption.targetCanvas.drawBitmap(center, null, dst, selectorFill2);
    }

    @Override
    public List<ColorElement> getColorElements(ColorPickerView.COLOR_SELECTION colorSelection, float x, float y) {
        List<ColorElement> colors = new ArrayList<>();
        ColorElement single = findNearestByPosition(x, y);
        if(single ==  null){
            return colors;
        }

        colors.add(single);
        int size = colorElementList.size();
        int index = colorElementList.indexOf(single);
        int divider = -1;
        switch (colorSelection) {
            case SINGLE:
                divider = 1;
                break;
            case COMPLEMENTARY:
                divider = 2;
                break;
            case TRIAD:
                divider = 3;
                break;
            case ANALOGOUS:
                if(size >= 3) {
                    colors.add(colorElementList.get(getRealIndex(index - 1, size)));
                    colors.add(colorElementList.get(getRealIndex(index + 1, size)));
                    return colors;
                }
                break;
            case SQUARE:
                divider = 4;
                break;
            default:
                break;
        }

        for(int elementIndex : getIndices(index, size, divider)) {
            colors.add(colorElementList.get(elementIndex));
        }

        return colors;
    }

    private int[] getIndices(int index, int size, int divider) {
        if(index >= size || divider <= 0){
            return new int[]{};
        }

        if(size % divider == 0) {
            int delta = size / divider;
            int[] indices = new int[divider - 1];
            for(int i = 1; i < divider; i++) {
                int newIndex = getRealIndex(index + delta * i, size);
                indices[i - 1] = newIndex;
            }
            return indices;
        }

        return new int[]{};
    }

    private int getRealIndex(int index, int size) {
        int realIndex = index;
        if(realIndex >= size) {
            realIndex -= size;
        } else if(realIndex < 0) {
            while (realIndex < 0) {
                realIndex = size + realIndex;
            }
        }
        return realIndex;
    }

    private ColorElement findNearestByPosition(float x, float y) {
        ColorElement near = null;
        double minDist = Double.MAX_VALUE;

        for (ColorElement colorElement : colorElementList) {
            double dist = colorElement.sqDist(x, y);
            if (minDist > dist) {
                minDist = dist;
                near = colorElement;
            }
        }

        return near;
    }
}
