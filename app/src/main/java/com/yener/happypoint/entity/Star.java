package com.yener.happypoint.entity;

/**
 * Created by erhan on 21.1.2018.
 */

public class Star {
    private String starId;
    private String starName;
    private Double latitude;
    private Double longitude;

    public Star() {
    }

    public Star(String starId, String starName, Double latitude, Double longitude) {
        this.starId = starId;
        this.starName = starName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStarId() {
        return starId;
    }

    public void setStarId(String starId) {
        this.starId = starId;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
