package com.keemsa.seasonify.features.colorwheel;

import android.graphics.Color;

/**
 * Created by sebastian on 04/07/17.
 */
// based on https://github.com/QuadFlask/colorpicker/blob/master/library/src/main/java/com/flask/colorpicker/ColorCircle.java
public class ColorElement {
    private float x, y, angle, sweepAngle;
    private float[] hsv = new float[3];
    private float[] hsvClone;
    private int color;

    public ColorElement(float x, float y, float angle, float sweepAngle, float[] hsv) {
        set(x, y, angle, sweepAngle, hsv);
    }

    public double sqDist(float x, float y) {
        double dx = this.x - x;
        double dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getSweepAngle() {
        return sweepAngle;
    }

    public void setSweepAngle(float sweepAngle) {
        this.sweepAngle = sweepAngle;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float[] getHsv() {
        return hsv;
    }

    public float[] getHsvWithLightness(float lightness) {
        if (hsvClone == null)
            hsvClone = hsv.clone();
        hsvClone[0] = hsv[0];
        hsvClone[1] = hsv[1];
        hsvClone[2] = lightness;
        return hsvClone;
    }

    public void set(float x, float y, float angle, float sweepAngle, float[] hsv) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.sweepAngle = sweepAngle;
        this.hsv[0] = hsv[0];
        this.hsv[1] = hsv[1];
        this.hsv[2] = hsv[2];
        this.color = Color.HSVToColor(this.hsv);
    }

    public int getColor() {
        return color;
    }
}
