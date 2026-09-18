package com.railsync.model;

import java.time.LocalTime;

/**
 * Semi-high speed express train.
 */
public class VandeBharatExpress extends Train {
    private static final long serialVersionUID = 1L;

    public VandeBharatExpress(String trainNumber, String trainName, Station source, Station destination,
                              LocalTime departureTime, LocalTime arrivalTime, double distanceKm) {
        super(trainNumber, trainName, source, destination, departureTime, arrivalTime, distanceKm, 1.10);
    }

    @Override
    public String getTrainType() {
        return "Vande Bharat Express";
    }

    @Override
    public double getSurcharge() {
        return 260.0;
    }

    @Override
    public double getSpeedKmph() {
        return 160.0;
    }

    @Override
    public boolean isCateringIncluded() {
        return true;
    }

    @Override
    public int getPriorityLevel() {
        return 1;
    }
}
