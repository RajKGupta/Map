package com.example.rajk.geofiretrial3.model;

/**
 * Created by RajK on 05-12-2017.
 */

public class PersonalDetails {
    private String name,phone,blood,address,gender,age,diseases,imgurl,email,id;
    private Boolean panic;

    public Boolean getPanic() {
        return panic;
    }

    public void setPanic(Boolean panic) {
        this.panic = panic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PersonalDetails() {
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

    public PersonalDetails(String name, String phone, String blood, String address, String gender, String age, String diseases, String imgurl, String email,String id) {
        this.name = name;
        this.phone = phone;
        this.blood = blood;
        this.address = address;
        this.gender = gender;
        this.age = age;
        this.diseases = diseases;
        this.imgurl = imgurl;
        this.email = email;
        this.id=id;
        this.panic=false;
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
}
