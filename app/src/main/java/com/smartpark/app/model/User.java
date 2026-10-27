package com.smartpark.app.model;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String name;
    private String email;
    private String mobile;
    private String vehicle;
    private String city;

    public User() {
    }

    public User(String uid, String name, String email, String mobile, String vehicle, String city) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.vehicle = vehicle;
        this.city = city;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getVehicle() { return vehicle; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
