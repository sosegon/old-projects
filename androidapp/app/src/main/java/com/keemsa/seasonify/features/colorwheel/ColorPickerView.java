package com.keemsa.seasonify.features.colorwheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.keemsa.seasonify.R;

import java.util.ArrayList;

/**
 * Created by sebastian on 04/07/17.
 */

public class ColorPickerView extends View {
    private static final String LOG_TAG = ColorPickerView.class.getSimpleName();

    private Bitmap colorWheel, centerWheel;
    private Canvas colorWheelCanvas, centerWheelCanvas;
    private float innerRadius = 0.75f; // percentage
    private float strokeWidth = 4f;

    private int backgroundColor = 0x145632;
    private ColorElement currentColorElement;

    private ArrayList<OnColorChangedListener> colorChangedListeners = new ArrayList<>();
    private ArrayList<OnColorSelectedListener> listeners = new ArrayList<>();

    private Paint colorWheelFill = PaintBuilder.newPaint().color(0).build();
    private Paint selectorOutterStroke = PaintBuilder.newPaint().color(0xff000000).stroke(strokeWidth).style(Paint.Style.STROKE).build();
    private Paint selectorInnerStroke = PaintBuilder.newPaint().color(0x7fffffff).stroke(strokeWidth).style(Paint.Style.STROKE).build();

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
        canvas.drawColor(backgroundColor, PorterDuff.Mode.LIGHTEN);
        if (colorWheel != null)
            canvas.drawBitmap(colorWheel, 0, 0, null);
        if (currentColorElement != null) {
            float half = canvas.getWidth() / 2f;

            float startAngle = currentColorElement.getAngle();
            float sweepAngle = currentColorElement.getSweepAngle();

            float gap = strokeWidth * 0.5f;
            // the gap is half the stroke because the stroke of a
            // shape is right in the middle of the boundaries

            canvas.drawArc(gap,
                            gap,
                            canvas.getWidth() - gap,
                            canvas.getHeight() - gap,
                            startAngle, sweepAngle, false, selectorOutterStroke);

            gap = strokeWidth * 1.5f;
            // the gap is one and a half the stroke because the stroke of a
            // shape is right in the middle of the boundaries

            canvas.drawArc(gap,
                    gap,
                    canvas.getWidth() - gap,
                    canvas.getHeight() - gap,
                    startAngle, sweepAngle, false, selectorInnerStroke);

            // Circle to create effect of blank space in the middle of the color wheel
            colorWheelFill.setColor(0xffffff);
            float blankRadius = innerRadius * renderer.getRenderOption().radius;
            canvas.drawCircle(half, half, blankRadius, colorWheelFill);
        }
        if (centerWheel != null)
            canvas.drawBitmap(centerWheel, 0, 0, null);
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
                if(!isInnerArea(event.getX(), event.getY())) {
                    currentColorElement = findNearestByPosition(event.getX(), event.getY());
                    int selectedColor = getSelectedColor();

                    callOnColorChangedListeners(lastSelectedColor, selectedColor);

                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
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

    private boolean isInnerArea(float x, float y) {
        float centerX = colorWheelCanvas.getWidth() / 2;
        float centerY = colorWheelCanvas.getHeight() / 2;
        float dx = Math.abs(x - centerX);
        float dy = Math.abs(y - centerY);

        float distToCenter = (float)Math.sqrt(dx * dx + dy * dy);
        float radius = (centerX - strokeWidth) * innerRadius;

        return distToCenter <= radius;

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

    public void updateCenter(Bitmap bitmap) {
        renderer.setCenter(bitmap);
        updateCenterWheel();
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
        float radius = half;

        ColorWheelRenderOption colorWheelRenderOption = renderer.getRenderOption();
        colorWheelRenderOption.radius = radius;
        colorWheelRenderOption.innerRadius = innerRadius;
        colorWheelRenderOption.strokeWidth = strokeWidth;
        colorWheelRenderOption.targetCanvas = colorWheelCanvas;

        renderer.initWith(colorWheelRenderOption);
        renderer.drawColor();
    }

    private void updateCenterWheel() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (height < width)
            width = height;
        if (width <= 0)
            return;
        if (centerWheel == null) {
            centerWheel = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            centerWheelCanvas = new Canvas(centerWheel);
        }
        drawCenterWheel();
        invalidate();
    }

    private void drawCenterWheel() {
        centerWheelCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        if (renderer == null) return;

        float half = colorWheelCanvas.getWidth() / 2f;
        float radius = half;

        ColorWheelRenderOption colorWheelRenderOption = renderer.getRenderOption();
        colorWheelRenderOption.radius = radius;
        colorWheelRenderOption.innerRadius = innerRadius;
        colorWheelRenderOption.strokeWidth = strokeWidth;
        colorWheelRenderOption.targetCanvas = centerWheelCanvas;

        renderer.initWith(colorWheelRenderOption);
        renderer.drawCenter();
    }

    public enum WHEEL_TYPE {
        CIRCLE, ARC;

        public static WHEEL_TYPE indexOf(int index) {
            switch (index) {
                case 0:
                    return CIRCLE;
                case 1:
                    return ARC;
            }
            return CIRCLE;
        }
    }
}
