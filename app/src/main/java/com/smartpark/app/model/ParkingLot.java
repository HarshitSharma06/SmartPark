package com.smartpark.app.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;


public class ParkingLot implements Serializable {
    private String lotId;
    private String name;
    private String address;
    private String city;
    private double lat;
    private double lng;
    private int pricePerHour;
    private int totalSlots;
    private int freeSlots;
    private String status; // "AVAILABLE", "FILLING", "FULL"
    private boolean covered;
    private boolean ev;
    private String openHours;
    private String phone;
    private String vehicleTypes;
    private String photoUrl;

    // Computed / transient properties
    private double distanceKm;
    private int aiChance;

    // Empty constructor required for Firebase Realtime Database deserialization
    public ParkingLot() {
    }

    public ParkingLot(String lotId, String name, String address, String city, double lat, double lng,
                      int pricePerHour, int totalSlots, int freeSlots, String status,
                      boolean covered, boolean ev, String openHours, String phone,
                      String vehicleTypes, String photoUrl) {
        this.lotId = lotId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.lat = lat;
        this.lng = lng;
        this.pricePerHour = pricePerHour;
        this.totalSlots = totalSlots;
        this.freeSlots = freeSlots;
        this.status = status;
        this.covered = covered;
        this.ev = ev;
        this.openHours = openHours;
        this.phone = phone;
        this.vehicleTypes = vehicleTypes;
        this.photoUrl = photoUrl;
    }

    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public int getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(int pricePerHour) { this.pricePerHour = pricePerHour; }

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

    public int getFreeSlots() { return freeSlots; }
    public void setFreeSlots(int freeSlots) { this.freeSlots = freeSlots; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isCovered() { return covered; }
    public void setCovered(boolean covered) { this.covered = covered; }

    public boolean isEv() { return ev; }
    public void setEv(boolean ev) { this.ev = ev; }

    public String getOpenHours() { return openHours; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVehicleTypes() { return vehicleTypes; }
    public void setVehicleTypes(String vehicleTypes) { this.vehicleTypes = vehicleTypes; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getAiChance() { return aiChance; }
    public void setAiChance(int aiChance) { this.aiChance = aiChance; }

    @Exclude
    public String getFormattedPrice() {
        return "₹" + pricePerHour + "/hr";
    }

    @Exclude
    public String getFormattedDistance() {
        if (distanceKm < 1.0) {
            return (int) (distanceKm * 1000) + " m";
        } else {
            return String.format(java.util.Locale.US, "%.1f km", distanceKm);
        }
    }
}
