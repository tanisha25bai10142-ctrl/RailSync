package com.railsync.model;

import java.time.LocalTime;

/**
 * Long-distance Superfast train with general, sleeper, and AC coaches.
 */
public class SuperfastExpress extends Train {
    private static final long serialVersionUID = 1L;

    public SuperfastExpress(String trainNumber, String trainName, Station source, Station destination,
                            LocalTime departureTime, LocalTime arrivalTime, double distanceKm) {
        super(trainNumber, trainName, source, destination, departureTime, arrivalTime, distanceKm, 0.65);
    }

    @Override
    public String getTrainType() {
        return "Superfast Express";
    }

    @Override
    public double getSurcharge() {
        return 45.0; // Standard superfast surcharge
    }

    @Override
    public double getSpeedKmph() {
        return 95.0;
    }

    @Override
    public boolean isCateringIncluded() {
        return false;
    }

    @Override
    public int getPriorityLevel() {
        return 3;
    }
}
