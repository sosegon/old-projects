package com.keemsa.seasonify.features.colorwheel;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * Created by sebastian on 04/07/17.
 */

public class SimpleColorWheelRenderer extends AbsColorWheelRenderer {
    private Paint selectorFill = PaintBuilder.newPaint().antiAlias(true).build();
    private float[] hsv = new float[3];

    @Override
    public void drawColor() {
        int count = colorList.size();
        if(count == 0){
            return;
        }

        colorElementList.clear();

        float sweepAngle = 360 / count; // degrees

        float half = colorWheelRenderOption.targetCanvas.getWidth() / 2f;
        float radius = colorWheelRenderOption.radius;
        float innerRadius = colorWheelRenderOption.innerRadius * radius;

        RectF oval = Utils.getRectF(colorWheelRenderOption);

        for(int i = 0; i < count; i++) {
            float angle = sweepAngle * i;
            float radAngle = (float)(angle * Math.PI / 180); // needed for trigonometric operations
            float x = half + (float) (radius * Math.cos(radAngle));
            float y = half + (float) (radius * Math.sin(radAngle));

            hsv = colorList.get(i);
            selectorFill.setColor(Color.HSVToColor(hsv));
            colorWheelRenderOption.targetCanvas.drawArc(oval, angle, sweepAngle, true, selectorFill);
            colorElementList.add(new ColorElement(x, y, angle, sweepAngle, hsv));
        }

        // Circle to create effect of blank space in the middle of the color wheel
        selectorFill.setColor(Color.parseColor("#ffffff"));
        colorWheelRenderOption.targetCanvas.drawCircle(half, half, innerRadius, selectorFill);
    }

    @Override
    public void drawCenter() {
        if(center == null)
            return;

        float half = colorWheelRenderOption.targetCanvas.getWidth() / 2f;
        float radius = colorWheelRenderOption.radius;
        float innerRadius = colorWheelRenderOption.innerRadius * radius;

        float x = half - innerRadius;
        float y = x;

        colorWheelRenderOption.targetCanvas.drawCircle(half, half, innerRadius, selectorFill);
        selectorFill.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        RectF dst = new RectF(x, y, x + 2 * innerRadius, y + 2 * innerRadius);
        colorWheelRenderOption.targetCanvas.drawBitmap(center, null, dst, selectorFill);
    }
}
