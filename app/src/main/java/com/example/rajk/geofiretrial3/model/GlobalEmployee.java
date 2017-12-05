package com.example.rajk.geofiretrial3.model;


public class GlobalEmployee {

    public GlobalEmployee(){

    }

    private String name;
    private String phone_num;
    private String address;
    private String designation;
    private String username;
    private String lastSeen;

    public GlobalEmployee(String name, String phone_num, String address, String designation, String username, String lastSeen) {
        this.name = name;
        this.phone_num = phone_num;
        this.address = address;
        this.designation = designation;
        this.username = username;
        this.lastSeen = lastSeen;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }


}
