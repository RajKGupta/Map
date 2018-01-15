package com.example.rajk.geofiretrial3.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.rajk.geofiretrial3.SaferIndia;

public class SharedPreference {
    private Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int mode = 0;
    String prefname = "SESSION";

    public Boolean getLoggedIn() {
        return pref.getBoolean(SaferIndia.loggedIn, false);
    }


    public SharedPreference(Context context)
    {
        this.context = context;
        pref = context.getSharedPreferences(prefname, mode);
        editor = pref.edit();
    }

    public String getUID() {
        return pref.getString(SaferIndia.UID, "");
    }


    public String getName() {
        return pref.getString(SaferIndia.name, "");
    }


    public String getPhone() {
        return pref.getString(SaferIndia.phone, "");
    }


    public String getBlood() {
        return pref.getString(SaferIndia.blood, "");
    }


    public String getAddress() {
        return pref.getString(SaferIndia.address, "");
    }


    public String getGender() {
        return pref.getString(SaferIndia.gender, "");
    }


    public String getAge() {
        return pref.getString(SaferIndia.age, "");
    }


    public String getDiseases() {
        return pref.getString(SaferIndia.diseases, "");
    }


    public String getImgurl() {
        return pref.getString(SaferIndia.imgurl, "");
    }


    public String getEmail() {
        return pref.getString(SaferIndia.email, "");
    }
    public String getPin() {
        return pref.getString(SaferIndia.pin, "");
    }

    public void setEmail(String email) {
        editor.putString(SaferIndia.email,email);
        editor.commit();
    }
    public void setImgurl(String imgurl) {
        editor.putString(SaferIndia.imgurl,imgurl);
        editor.commit();
    }
    public void setUID(String UID) {
        editor.putString(SaferIndia.UID,UID);
        editor.commit();
    }
    public void setName(String name) {
        editor.putString(SaferIndia.name,name);
        editor.commit();
    }
    public void setPin(String pin) {
        editor.putString(SaferIndia.pin,pin);
        editor.commit();
    }
    public Boolean getShareLocation() {
        return pref.getBoolean(SaferIndia.shareLocation, true);
    }

    public void setShareLocation(Boolean shareLocation) {
        editor.putBoolean(SaferIndia.shareLocation,shareLocation);
        editor.commit();

    }

    public Boolean getPanick() {
        return pref.getBoolean(SaferIndia.panick,true);

    }

    public void setPanick(Boolean panick) {
        editor.putBoolean(SaferIndia.panick,panick);
        editor.commit();
    }

    public Boolean getAlarmSound() {
        return pref.getBoolean(SaferIndia.alarmSound,false);
    }

    public void setAlarmSound(Boolean alarmSound) {
        editor.putBoolean(SaferIndia.alarmSound,alarmSound);
        editor.commit();
    }

    public void setSharedPreference(String name, String phone, String blood, String address, String gender, String age, String diseases, String imgurl, String email, String pin) {
        editor.putString(SaferIndia.name,name);
        editor.putString(SaferIndia.phone,phone);
        editor.putString(SaferIndia.blood,blood);
        editor.putString(SaferIndia.address,address);
        editor.putString(SaferIndia.gender,gender);
        editor.putString(SaferIndia.age,age);
        editor.putString(SaferIndia.diseases,diseases);
        editor.putString(SaferIndia.imgurl,imgurl);
        editor.putString(SaferIndia.email,email);
        editor.putBoolean(SaferIndia.alarmSound,false);
        editor.putBoolean(SaferIndia.panick,false);
        editor.putBoolean(SaferIndia.shareLocation,true);
        editor.putBoolean(SaferIndia.loggedIn,true);
        editor.putString(SaferIndia.pin,pin);
        editor.commit();
    }

    public String getFCMavail() {
        return pref.getString("FCM",null);
    }

    public void setFCMavail(String panick) {
        editor.putString("FCM",panick);
        editor.commit();
    }

    public void setfirst(Boolean isfirst)
    {
        editor.putBoolean("check",isfirst);
        editor.commit();
    }

    public Boolean check()
    {
        return pref.getBoolean("check",true);
    }


}
