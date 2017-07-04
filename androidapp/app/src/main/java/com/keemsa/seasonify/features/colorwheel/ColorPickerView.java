package com.keemsa.seasonify.features.colorwheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.keemsa.seasonify.R;

/**
 * Created by sebastian on 04/07/17.
 */

public class ColorPickerView extends View {
    private static final float STROKE_RATIO = 2f;

    private Bitmap colorWheel;
    private int density = 10;

    private float lightness = 1;
    private float alpha = 1;
    private int backgroundColor = 0x00000000;

    private Integer initialColors[] = new Integer[]{null, null, null, null, null};
    private int colorSelection = 0;
    private Integer initialColor;
    private Integer pickerTextColor;
    private ColorCircle currentColorCircle;

    private Paint colorWheelFill = PaintBuilder.newPaint().color(0).build();
    private Paint selectorStroke1 = PaintBuilder.newPaint().color(0xffffffff).build();
    private Paint selectorStroke2 = PaintBuilder.newPaint().color(0xff000000).build();
    private Paint alphaPatternPaint = PaintBuilder.newPaint().build();

    private EditText colorEdit;
    private LinearLayout colorPreview;

    private ColorWheelRenderer renderer;

    private int alphaSliderViewId, lightnessSliderViewId;

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        if (currentColorCircle != null) {
            float maxRadius = canvas.getWidth() / 2f - STROKE_RATIO * (1f + ColorWheelRenderer.GAP_PERCENTAGE);
            float size = maxRadius / density / 2;
            colorWheelFill.setColor(Color.HSVToColor(currentColorCircle.getHsvWithLightness(this.lightness)));
            colorWheelFill.setAlpha((int) (alpha * 0xff));
            canvas.drawCircle(currentColorCircle.getX(), currentColorCircle.getY(), size * STROKE_RATIO, selectorStroke1);
            canvas.drawCircle(currentColorCircle.getX(), currentColorCircle.getY(), size * (1 + (STROKE_RATIO - 1) / 2), selectorStroke2);

            canvas.drawCircle(currentColorCircle.getX(), currentColorCircle.getY(), size, alphaPatternPaint);
            canvas.drawCircle(currentColorCircle.getX(), currentColorCircle.getY(), size, colorWheelFill);
        }
    }

    public void setDensity(int density) {
        this.density = Math.max(2, density);
        invalidate();
    }

    public void setRenderer(ColorWheelRenderer renderer) {
        this.renderer = renderer;
        invalidate();
    }

    public void setInitialColor(int color, boolean updateText) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        this.alpha = Utils.getAlphaPercent(color);
        this.lightness = hsv[2];
        this.initialColors[this.colorSelection] = color;
        this.initialColor = color;
        setColorPreviewColor(color);
        currentColorCircle = findNearestByColor(color);
    }

    private ColorCircle findNearestByColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        ColorCircle near = null;
        double minDiff = Double.MAX_VALUE;
        double x = hsv[1] * Math.cos(hsv[0] * Math.PI / 180);
        double y = hsv[1] * Math.sin(hsv[0] * Math.PI / 180);

        for (ColorCircle colorCircle : renderer.getColorCircleList()) {
            float[] hsv1 = colorCircle.getHsv();
            double x1 = hsv1[1] * Math.cos(hsv1[0] * Math.PI / 180);
            double y1 = hsv1[1] * Math.sin(hsv1[0] * Math.PI / 180);
            double dx = x - x1;
            double dy = y - y1;
            double dist = dx * dx + dy * dy;
            if (dist < minDiff) {
                minDiff = dist;
                near = colorCircle;
            }
        }

        return near;
    }

    private void setColorPreviewColor(int newColor) {
        if (colorPreview == null || initialColors == null || colorSelection > initialColors.length || initialColors[colorSelection] == null)
            return;

        int children = colorPreview.getChildCount();
        if (children == 0 || colorPreview.getVisibility() != View.VISIBLE)
            return;

        View childView = colorPreview.getChildAt(colorSelection);
        if (!(childView instanceof LinearLayout))
            return;
        LinearLayout childLayout = (LinearLayout) childView;
        ImageView childImage = (ImageView) childLayout.findViewById(R.id.image_preview);
        childImage.setImageDrawable(new CircleColorDrawable(newColor));
    }

    private void initWith(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);

        density = typedArray.getInt(R.styleable.ColorPickerPreference_density, 10);
        initialColor = typedArray.getInt(R.styleable.ColorPickerPreference_initialColor, 0xffffffff);

        pickerTextColor = typedArray.getInt(R.styleable.ColorPickerPreference_pickerColorEditTextColor, 0xffffffff);

        WHEEL_TYPE wheelType = WHEEL_TYPE.indexOf(typedArray.getInt(R.styleable.ColorPickerPreference_wheelType, 0));
        ColorWheelRenderer renderer = ColorWheelRendererBuilder.getRenderer(wheelType);

        alphaSliderViewId = typedArray.getResourceId(R.styleable.ColorPickerPreference_alphaSliderView, 0);
        lightnessSliderViewId = typedArray.getResourceId(R.styleable.ColorPickerPreference_lightnessSliderView, 0);

        setRenderer(renderer);
        setDensity(density);
        setInitialColor(initialColor, true);

        typedArray.recycle();
    }

    public enum WHEEL_TYPE {
        FLOWER, CIRCLE;

        public static WHEEL_TYPE indexOf(int index) {
            switch (index) {
                case 0:
                    return FLOWER;
                case 1:
                    return CIRCLE;
            }
            return FLOWER;
        }
    }
}
