package com.example.rajk.geofiretrial3.model;

/**
 * Created by RajK on 15-05-2017.
 */

public class Distance {
    private int distInMetre;
    private String name;
    private String picture;
    private String id;
    private int color = -1;

    public Distance() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getDistInMetre() {
        return distInMetre;
    }

    public void setDistInMetre(int distInMetre) {
        this.distInMetre = distInMetre;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
