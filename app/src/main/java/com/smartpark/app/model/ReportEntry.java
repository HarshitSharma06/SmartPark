package com.smartpark.app.model;

import java.io.Serializable;


public class ReportEntry implements Serializable {
    private String pushId;
    private String lotId;
    private String uid;
    private String status;
    private String observedAt;
    private String note;
    private double lat;
    private double lng;
    private String photoLocalPath;

    public ReportEntry() {
    }

    public ReportEntry(String pushId, String lotId, String uid, String status, String observedAt,
                       String note, double lat, double lng, String photoLocalPath) {
        this.pushId = pushId;
        this.lotId = lotId;
        this.uid = uid;
        this.status = status;
        this.observedAt = observedAt;
        this.note = note;
        this.lat = lat;
        this.lng = lng;
        this.photoLocalPath = photoLocalPath;
    }

    public String getPushId() { return pushId; }
    public void setPushId(String pushId) { this.pushId = pushId; }

    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservedAt() { return observedAt; }
    public void setObservedAt(String observedAt) { this.observedAt = observedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getPhotoLocalPath() { return photoLocalPath; }
    public void setPhotoLocalPath(String photoLocalPath) { this.photoLocalPath = photoLocalPath; }
}
