package com.example.rajk.geofiretrial3.model;

/**
 * Created by RajK on 05-12-2017.
 */

public class Online {
    private Boolean online;
    private String lastSeen;
    private String phone;
    private String name;

    public String getName() {
        return name;
    }

    public Online(Boolean online, String lastSeen, String phone, String name) {
        this.online = online;
        this.lastSeen = lastSeen;
        this.phone = phone;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Online() {
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
