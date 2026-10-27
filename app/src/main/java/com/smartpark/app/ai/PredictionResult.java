package com.smartpark.app.ai;

import java.io.Serializable;
public class PredictionResult implements Serializable {
    private final int chancePercent;
    private final int expectedFreeSlotsMin;
    private final int expectedFreeSlotsMax;
    private final String expectedSearchTime;
    private final String recommendation;
    private final double predictedOccupancy;

    public PredictionResult(int chancePercent, int expectedFreeSlotsMin, int expectedFreeSlotsMax,
                            String expectedSearchTime, String recommendation, double predictedOccupancy) {
        this.chancePercent = chancePercent;
        this.expectedFreeSlotsMin = Math.max(0, expectedFreeSlotsMin);
        this.expectedFreeSlotsMax = Math.max(expectedFreeSlotsMin, expectedFreeSlotsMax);
        this.expectedSearchTime = expectedSearchTime;
        this.recommendation = recommendation;
        this.predictedOccupancy = predictedOccupancy;
    }

    public int getChancePercent() { return chancePercent; }
    public int getExpectedFreeSlotsMin() { return expectedFreeSlotsMin; }
    public int getExpectedFreeSlotsMax() { return expectedFreeSlotsMax; }
    public String getExpectedSearchTime() { return expectedSearchTime; }
    public String getRecommendation() { return recommendation; }
    public double getPredictedOccupancy() { return predictedOccupancy; }

    public String getFreeSlotsRangeString() {
        if (expectedFreeSlotsMin == expectedFreeSlotsMax) {
            return String.valueOf(expectedFreeSlotsMin);
        }
        return expectedFreeSlotsMin + "-" + expectedFreeSlotsMax;
    }
}
