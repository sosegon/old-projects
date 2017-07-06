package com.keemsa.seasonify.features.colorwheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.keemsa.seasonify.R;

import java.util.ArrayList;

/**
 * Created by sebastian on 04/07/17.
 */

public class ColorPickerView extends View {
    private static final String LOG_TAG = ColorPickerView.class.getSimpleName();
    private static final float STROKE_RATIO = 2f;

    private Bitmap colorWheel;
    private Canvas colorWheelCanvas;
    private float innerRadius = 0.75f; // percentage
    private float strokeWidth = 5f;

    private int backgroundColor = 0xffffff;
    private ColorElement currentColorElement;

    private ArrayList<OnColorChangedListener> colorChangedListeners = new ArrayList<>();
    private ArrayList<OnColorSelectedListener> listeners = new ArrayList<>();

    private Paint colorWheelFill = PaintBuilder.newPaint().color(0).build();
    private Paint selectorStroke = PaintBuilder.newPaint().color(0xffffffff).stroke(strokeWidth).style(Paint.Style.STROKE).build();

    private ColorWheelRenderer renderer;

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWith(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = 0;
        if (widthMode == MeasureSpec.UNSPECIFIED)
            width = widthMeasureSpec;
        else if (widthMode == MeasureSpec.AT_MOST)
            width = MeasureSpec.getSize(widthMeasureSpec);
        else if (widthMode == MeasureSpec.EXACTLY)
            width = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = 0;
        if (heightMode == MeasureSpec.UNSPECIFIED)
            height = widthMeasureSpec;
        else if (heightMode == MeasureSpec.AT_MOST)
            height = MeasureSpec.getSize(heightMeasureSpec);
        else if (widthMode == MeasureSpec.EXACTLY)
            height = MeasureSpec.getSize(heightMeasureSpec);
        int squareDimen = width;
        if (height < width)
            squareDimen = height;
        setMeasuredDimension(squareDimen, squareDimen);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(backgroundColor);
        if (colorWheel != null)
            canvas.drawBitmap(colorWheel, 0, 0, null);
        if (currentColorElement != null) {
            float half = canvas.getWidth() / 2f;

            float startAngle = currentColorElement.getAngle();
            float sweepAngle = currentColorElement.getSweepAngle();

            canvas.drawArc(strokeWidth,
                            strokeWidth,
                            canvas.getWidth() - strokeWidth,
                            canvas.getHeight() - strokeWidth,
                            startAngle, sweepAngle, true, selectorStroke);

            // Circle to create effect of blank space in the middle of the color wheel
            colorWheelFill.setColor(Color.parseColor("#ffffff"));
            float blankRadius = innerRadius * renderer.getRenderOption().radius;
            canvas.drawCircle(half, half, blankRadius, colorWheelFill);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateColorWheel();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                int lastSelectedColor = getSelectedColor();
                currentColorElement = findNearestByPosition(event.getX(), event.getY());
                int selectedColor = getSelectedColor();

                Log.e(LOG_TAG, "action move: " + currentColorElement.getAngle());

                callOnColorChangedListeners(lastSelectedColor, selectedColor);

                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                Log.e(LOG_TAG, "action up");
                int selectedColor = getSelectedColor();
                if (listeners != null) {
                    for (OnColorSelectedListener listener : listeners) {
                        try {
                            listener.onColorSelected(selectedColor);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                invalidate();
                break;
            }
        }
        return true;
    }

    protected void callOnColorChangedListeners(int oldColor, int newColor) {
        if (colorChangedListeners != null && oldColor != newColor) {
            for (OnColorChangedListener listener : colorChangedListeners) {
                try {
                    listener.onColorChanged(newColor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ColorElement findNearestByPosition(float x, float y) {
        ColorElement near = null;
        double minDist = Double.MAX_VALUE;

        for (ColorElement colorElement : renderer.getColorElementList()) {
            double dist = colorElement.sqDist(x, y);
            if (minDist > dist) {
                minDist = dist;
                near = colorElement;
            }
        }

        return near;
    }

    public int getSelectedColor() {
        int color = 0;
        if (currentColorElement != null)
            color = Utils.colorAtLightness(currentColorElement.getColor(), 1);
        return Utils.adjustAlpha(1, color);
    }

    public void updateColors(int[] colors) {
        renderer.updateColorList(colors);
        updateColorWheel();
    }

    public void setRenderer(ColorWheelRenderer renderer) {
        this.renderer = renderer;
        invalidate();
    }

    private void initWith(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);

        innerRadius = typedArray.getFloat(R.styleable.ColorPickerPreference_innerRadius, 0.75f);

        WHEEL_TYPE wheelType = WHEEL_TYPE.indexOf(typedArray.getInt(R.styleable.ColorPickerPreference_wheelType, 0));
        ColorWheelRenderer renderer = ColorWheelRendererBuilder.getRenderer(wheelType);

        setRenderer(renderer);

        typedArray.recycle();
    }

    private void updateColorWheel() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (height < width)
            width = height;
        if (width <= 0)
            return;
        if (colorWheel == null) {
            colorWheel = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            colorWheelCanvas = new Canvas(colorWheel);
        }
        drawColorWheel();
        invalidate();
    }

    private void drawColorWheel() {
        colorWheelCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        if (renderer == null) return;

        float half = colorWheelCanvas.getWidth() / 2f;
        float radius = half - strokeWidth;

        ColorWheelRenderOption colorWheelRenderOption = renderer.getRenderOption();
        colorWheelRenderOption.radius = radius;
        colorWheelRenderOption.innerRadius = innerRadius;
        colorWheelRenderOption.strokeWidth = strokeWidth;
        colorWheelRenderOption.targetCanvas = colorWheelCanvas;

        renderer.initWith(colorWheelRenderOption);
        renderer.draw();
    }

    public enum WHEEL_TYPE {
        CIRCLE;

        public static WHEEL_TYPE indexOf(int index) {
            switch (index) {
                case 0:
                    return CIRCLE;
            }
            return CIRCLE;
        }
    }
}
