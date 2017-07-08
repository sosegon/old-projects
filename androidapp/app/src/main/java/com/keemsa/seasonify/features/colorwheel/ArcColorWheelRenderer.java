package com.keemsa.seasonify.features.colorwheel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.List;

/**
 * Created by sebastian on 04/07/17.
 */

public class ArcColorWheelRenderer extends AbsColorWheelRenderer {
    private Paint colorWheelFill = PaintBuilder.newPaint().color(0).build();
    private Paint selectorOuterStroke = PaintBuilder.newPaint().color(0xff000000).stroke(strokeWidth).style(Paint.Style.STROKE).build();
    private Paint selectorInnerStroke = PaintBuilder.newPaint().color(0x7fffffff).stroke(strokeWidth).style(Paint.Style.STROKE).build();
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
        float gap = colorWheelRenderOption.strokeWidth;
        float outerRadius = colorWheelRenderOption.radius - gap;
        float innerRadius = colorWheelRenderOption.innerRadiusRatio * outerRadius;
        float centerRadius = (outerRadius + innerRadius) / 2;

        RectF oval = Utils.getRectF(colorWheelRenderOption);

        for(int i = 0; i < count; i++) {
            float angle = sweepAngle * i;
            float radAngle = (float)(angle * Math.PI / 180); // needed for trigonometric operations
            float x = half + (float) (centerRadius * Math.cos(radAngle));
            float y = half + (float) (centerRadius * Math.sin(radAngle));

            hsv = colorList.get(i);
            selectorFill.setColor(Color.HSVToColor(hsv));
            colorWheelRenderOption.targetCanvas.drawArc(oval, angle, sweepAngle, true, selectorFill);
            colorElementList.add(new ColorElement(x, y, angle, sweepAngle, hsv));
        }

        // Circle to create effect of blank space in the middle of the color wheel
        selectorFill.setColor(0xffffffff);
        colorWheelRenderOption.targetCanvas.drawCircle(half, half, innerRadius, selectorFill);
    }

    @Override
    public void drawSelected(Canvas canvas, List<ColorElement> colors) {
        float half = canvas.getWidth() / 2f;

        for(ColorElement currentColorElement : colors) {
            float startAngle = currentColorElement.getAngle();
            float sweepAngle = currentColorElement.getSweepAngle();

            float gap = strokeWidth * 0.5f;
            // the gap is half the stroke because the stroke of a
            // shape is right in the middle of the boundaries

            canvas.drawArc(gap,
                    gap,
                    canvas.getWidth() - gap,
                    canvas.getHeight() - gap,
                    startAngle, sweepAngle, false, selectorOuterStroke);

            gap = strokeWidth * 1.5f;
            // the gap is one and a half the stroke because the stroke of a
            // shape is right in the middle of the boundaries

            canvas.drawArc(gap,
                    gap,
                    canvas.getWidth() - gap,
                    canvas.getHeight() - gap,
                    startAngle, sweepAngle, false, selectorInnerStroke);
        }


        // Circle to create effect of blank space in the middle of the color wheel
        colorWheelFill.setColor(0xffffffff);
        float gap = strokeWidth;
        float outerRadius = half - gap;
        float innerRadius = colorWheelRenderOption.innerRadiusRatio * outerRadius;
        canvas.drawCircle(half, half, innerRadius, colorWheelFill);
    }
}
