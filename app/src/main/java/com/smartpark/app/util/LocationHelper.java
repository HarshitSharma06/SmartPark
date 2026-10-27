package com.smartpark.app.util;

import android.location.Location;


public class LocationHelper {


    public static double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000.0;
    }


    public static int calculateEtaMinutes(double distanceKm) {
        if (distanceKm <= 0) return 2;
        return (int) Math.max(2, Math.round((distanceKm / 25.0) * 60.0));
    }
}
