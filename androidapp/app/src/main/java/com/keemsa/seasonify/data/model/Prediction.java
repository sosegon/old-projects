package com.keemsa.seasonify.data.model;

/**
 * Created by sebastian on 4/5/17.
 */

public class Prediction {

    private String season;
    private String photoUrl;
    private String userId;

    public Prediction(String prediction, String photoUrl, String userId) {
        this.season = prediction;
        this.photoUrl = photoUrl;
        this.userId = userId;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
