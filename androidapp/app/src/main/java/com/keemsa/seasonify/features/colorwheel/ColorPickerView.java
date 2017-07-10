package com.keemsa.seasonify.features.colorwheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.keemsa.seasonify.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastian on 04/07/17.
 */

public class ColorPickerView extends View {
    private static final String LOG_TAG = ColorPickerView.class.getSimpleName();

    private Bitmap colorWheel, centerWheel;
    private Canvas colorWheelCanvas, centerWheelCanvas;
    private float innerRadiusRatio = 0.75f; // radius / innerRadiusRatio
    private float strokeWidth = 4f;

    private int backgroundColor = 0x145632;
    private List<ColorElement> currentColorElements = new ArrayList<ColorElement>();

    private ArrayList<OnColorsChangedListener> colorChangedListeners = new ArrayList<>();
    private ArrayList<OnColorsSelectedListener> listeners = new ArrayList<>();


    private ColorWheelRenderer renderer;

    private COLOR_SELECTION colorSelection;

    public void setColorSelection(COLOR_SELECTION colorSelection) {
        this.colorSelection = colorSelection;
        invalidate();
    }

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
        if (currentColorElements != null && currentColorElements.size() > 0) {
            renderer.drawSelected(canvas, currentColorElements);
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
                if(isColorsArea(event.getX(), event.getY())) {
                    List<ColorElement> previousColorElements = currentColorElements;
                    currentColorElements = renderer.getColorElements(colorSelection, event.getX(), event.getY());
                    callOnColorChangedListeners(previousColorElements, currentColorElements);
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                currentColorElements = renderer.getColorElements(colorSelection, event.getX(), event.getY());
                if (listeners != null) {
                    for (OnColorsSelectedListener listener : listeners) {
                        try {
                            listener.onColorsSelected(currentColorElements);
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

    protected void callOnColorChangedListeners(List<ColorElement> oldColors, List<ColorElement> newColors) {
        if (colorChangedListeners != null && !oldColors.equals(newColors)) {
            for (OnColorsChangedListener listener : colorChangedListeners) {
                try {
                    listener.onColorsChanged(newColors);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isColorsArea(float x, float y) {
        float half = colorWheelCanvas.getWidth() / 2;
        float centerX = half;
        float centerY = half;
        float gap = strokeWidth;
        float outerRadius = half - gap;
        float innerRadius = innerRadiusRatio * outerRadius;

        float dx = Math.abs(x - centerX);
        float dy = Math.abs(y - centerY);

        float distToCenter = (float)Math.sqrt(dx * dx + dy * dy);

        return distToCenter >= innerRadius && distToCenter <= outerRadius;
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

        innerRadiusRatio = typedArray.getFloat(R.styleable.ColorPickerPreference_innerRadiusRatio, 0.75f);
        colorSelection = COLOR_SELECTION.indexOf(typedArray.getInt(R.styleable.ColorPickerPreference_colorSelection, 0));

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
        colorWheelRenderOption.innerRadiusRatio = innerRadiusRatio;
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
        colorWheelRenderOption.innerRadiusRatio = innerRadiusRatio;
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

    public enum COLOR_SELECTION {
        SINGLE, COMPLEMENTARY, TRIAD, ANALOGOUS, SQUARE;

        public static COLOR_SELECTION indexOf(int index) {
            switch (index) {
                case 0:
                    return SINGLE;
                case 1:
                    return COMPLEMENTARY;
                case 2:
                    return TRIAD;
                case 3:
                    return ANALOGOUS;
                case 4:
                    return SQUARE;
                default:
                    return SINGLE;
            }
        }
    }
}
