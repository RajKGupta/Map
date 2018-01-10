package com.example.rajk.geofiretrial3.model;

/**
 * Created by Soumya on 1/9/2018.
 */

public class gaurdians_and_responsibilities {

    private String phone, name, email, id, imgurl;

    public gaurdians_and_responsibilities(String phone,  String name, String email, String id,String imgurl) {
        this.phone = phone;
        this.name = name;
        this.email = email;
        this.id = id;
        this.imgurl = imgurl;
    }

    public gaurdians_and_responsibilities() {
    }

    public String getPhone() {
        return phone;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

