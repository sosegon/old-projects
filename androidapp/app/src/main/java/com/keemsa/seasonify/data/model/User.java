package com.keemsa.seasonify.data.model;

/**
 * Created by sebastian on 21/07/17.
 */

public class User {

    private String lastPrediction;

    public User(String lastPrediction) {
        this.lastPrediction = lastPrediction;
    }

    public String getLastPrediction() {
        return lastPrediction;
    }

    public void setLastPrediction(String lastPrediction) {
        this.lastPrediction = lastPrediction;
    }
}
