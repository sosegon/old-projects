package com.keemsa.seasonify.features.colorwheel;

/**
 * Created by sebastian on 04/07/17.
 */

public class ColorWheelRendererBuilder {
    public static ColorWheelRenderer getRenderer(ColorPickerView.WHEEL_TYPE wheelType) {
        switch (wheelType) {
            case ARC:
                return new ArcColorWheelRenderer();
        }
        throw new IllegalArgumentException("wrong WHEEL_TYPE");
    }
}
