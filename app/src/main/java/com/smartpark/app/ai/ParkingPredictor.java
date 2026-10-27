package com.smartpark.app.ai;

import com.smartpark.app.model.ParkingLot;

import java.util.Calendar;

public class ParkingPredictor {


    public static PredictionResult predict(ParkingLot lot, long arrivalTimeMillis) {
        if (lot == null) {
            return new PredictionResult(50, 0, 0, "5-10 min", "No data available.", 0.5);
        }

        int totalSlots = Math.max(1, lot.getTotalSlots());
        int freeSlots = Math.max(0, lot.getFreeSlots());
        double distanceKm = Math.max(0.0, lot.getDistanceKm());

        long now = System.currentTimeMillis();
        double gapMinutes = Math.max(0, (arrivalTimeMillis - now) / 60000.0);

        // Exponential decay weight: trust current state less as the time gap grows
        double w = Math.exp(-gapMinutes / 60.0);

        // Determine arrival hour and day type
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(arrivalTimeMillis);
        int arrivalHour = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

        double currentOcc = 1.0 - ((double) freeSlots / totalSlots);
        double historicalOcc = getHistoricalOccupancy(arrivalHour, isWeekend);

        // Weighted combination of real-time status and historical pattern
        double predictedOcc = w * currentOcc + (1.0 - w) * historicalOcc;

        // Calculate expected free slots range (+/- 15%)
        int expectedFreeCount = (int) Math.round(totalSlots * (1.0 - predictedOcc));
        int minFree = Math.max(0, (int) Math.floor(expectedFreeCount * 0.85));
        int maxFree = Math.min(totalSlots, (int) Math.ceil(expectedFreeCount * 1.15));

        // Chance formula: clamp( (1 - predictedOcc)*100 + 10 - 2*distanceKm, 5, 99 )
        double rawChance = (1.0 - predictedOcc) * 100.0 + 10.0 - (2.0 * distanceKm);
        int chancePercent = (int) Math.max(5, Math.min(99, Math.round(rawChance)));

        String searchTime;
        String recommendation;

        if (chancePercent >= 80) {
            searchTime = "2-5 min";
            recommendation = "Good chance of parking when you arrive.";
        } else if (chancePercent >= 50) {
            searchTime = "5-10 min";
            recommendation = "Parking is likely to become busy around your arrival time.";
        } else {
            searchTime = "10-20 min";
            recommendation = "Parking will probably be full - try an alternative.";
        }

        return new PredictionResult(chancePercent, minFree, maxFree, searchTime, recommendation, predictedOcc);
    }

    /**
     * Calculates prediction based on immediate ETA (distance / 25 km/h city speed).
     */
    public static PredictionResult predictImmediate(ParkingLot lot) {
        double distanceKm = lot != null ? lot.getDistanceKm() : 1.0;
        double etaMinutes = (distanceKm / 25.0) * 60.0;
        long arrivalTimeMillis = System.currentTimeMillis() + (long) (etaMinutes * 60000);
        return predict(lot, arrivalTimeMillis);
    }

    private static double getHistoricalOccupancy(int hour, boolean isWeekend) {
        if (isWeekend) {
            if (hour >= 12 && hour <= 22) return 0.85;
            if (hour >= 0 && hour <= 6) return 0.20;
            return 0.45;
        } else {
            if ((hour >= 11 && hour <= 14) || (hour >= 17 && hour <= 21)) return 0.80;
            if (hour >= 0 && hour <= 6) return 0.25;
            return 0.50;
        }
    }
}
