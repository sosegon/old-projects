package com.keemsa.seasonify.data.model;

/**
 * Created by sebastian on 22/07/17.
 */

public class Palette {

    private String colors;
    private String season;
    private String userId;
    private boolean isFav;

    public Palette() {
    }

    public Palette(String userId, String colors, String season, boolean isFav) {
        this.userId = userId;
        this.colors = colors;
        this.season = season;
        this.isFav = isFav;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }
}
