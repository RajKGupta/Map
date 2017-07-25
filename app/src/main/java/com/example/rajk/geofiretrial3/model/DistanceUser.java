package com.example.rajk.geofiretrial3.model;

/**
 * Created by RajK on 19-05-2017.
 */

public class DistanceUser {
   private String Name,Dist;

    public DistanceUser() {
    }

    public DistanceUser(String key, String m) {
        Name = key;
        Dist = m;

    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDist() {
        return Dist;
    }

    public void setDistance(String distance) {
        Dist = distance;
    }
}
