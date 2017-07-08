package com.keemsa.seasonify.features.colorwheel;

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
        float innerRadius = colorWheelRenderOption.innerRadius * radius;

        float x = half - innerRadius;
        float y = x;

        colorWheelRenderOption.targetCanvas.drawCircle(half, half, innerRadius, selectorFill2);
        selectorFill2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        RectF dst = new RectF(x, y, x + 2 * innerRadius, y + 2 * innerRadius);
        colorWheelRenderOption.targetCanvas.drawBitmap(center, null, dst, selectorFill2);
    }

}
