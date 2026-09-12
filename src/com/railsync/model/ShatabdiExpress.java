package com.railsync.model;

import java.time.LocalTime;

/**
 * Premium same-day intercity chair car express.
 */
public class ShatabdiExpress extends Train {
    private static final long serialVersionUID = 1L;

    public ShatabdiExpress(String trainNumber, String trainName, Station source, Station destination,
                           LocalTime departureTime, LocalTime arrivalTime, double distanceKm) {
        super(trainNumber, trainName, source, destination, departureTime, arrivalTime, distanceKm, 0.85);
    }

    @Override
    public String getTrainType() {
        return "Shatabdi Express";
    }

    @Override
    public double getSurcharge() {
        return 180.0;
    }

    @Override
    public double getSpeedKmph() {
        return 120.0;
    }

    @Override
    public boolean isCateringIncluded() {
        return true;
    }

    @Override
    public int getPriorityLevel() {
        return 2;
    }
}
