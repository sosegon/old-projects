package com.keemsa.seasonify.model;

/**
 * Created by sebastian on 4/5/17.
 */

public class Prediction {
    /*
        0 autumn
        1 spring
        2 summer
        3 winter
     */
    private int prediction;
    private String photoUrl;

    public Prediction(int prediction, String photoUrl) {
        this.prediction = prediction;
        this.photoUrl = photoUrl;
    }

    public int getPrediction() {
        return prediction;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
