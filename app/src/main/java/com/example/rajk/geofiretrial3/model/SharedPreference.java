package com.example.rajk.geofiretrial3.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    private Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int mode = 0;
    String prefname = "SESSION";
    private Boolean loggedIn;

    private String name,phone,blood,address,gender,age,diseases,imgurl,email;
    private Boolean shareLocation=true;
    private Boolean panick=false;
    private Boolean alarmSound=false;
    public SharedPreference(Context context) {
        this.context = context;
        pref = _context.getSharedPreferences(prefname, mode);
        editor = pref.edit();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBlood() {
        return blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDiseases() {
        return diseases;
    }

    public void setDiseases(String diseases) {
        this.diseases = diseases;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getShareLocation() {
        return shareLocation;
    }

    public void setShareLocation(Boolean shareLocation) {
        this.shareLocation = shareLocation;
    }

    public Boolean getPanick() {
        return panick;
    }

    public void setPanick(Boolean panick) {
        this.panick = panick;
    }

    public Boolean getAlarmSound() {
        return alarmSound;
    }

    public void setAlarmSound(Boolean alarmSound) {
        this.alarmSound = alarmSound;
    }

    public void setSharedPreference(String name, String phone, String blood, String address, String gender, String age, String diseases, String imgurl, String email) {
        this.name = name;
        this.phone = phone;
        this.blood = blood;
        this.address = address;
        this.gender = gender;
        this.age = age;
        this.diseases = diseases;
        this.imgurl = imgurl;
        this.email = email;
        loggedIn = true;
    }
}
