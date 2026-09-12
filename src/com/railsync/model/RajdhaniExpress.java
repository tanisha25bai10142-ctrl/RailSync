package com.railsync.model;

import java.time.LocalTime;

/**
 * Premium overnight high-speed train connecting national capital.
 * Demonstrates inheritance and method overriding.
 */
public class RajdhaniExpress extends Train {
    private static final long serialVersionUID = 1L;

    public RajdhaniExpress(String trainNumber, String trainName, Station source, Station destination,
                           LocalTime departureTime, LocalTime arrivalTime, double distanceKm) {
        super(trainNumber, trainName, source, destination, departureTime, arrivalTime, distanceKm, 0.95);
    }

    @Override
    public String getTrainType() {
        return "Rajdhani Express";
    }

    @Override
    public double getSurcharge() {
        return 220.0; // Premium catering + superfast surcharge
    }

    @Override
    public double getSpeedKmph() {
        return 130.0;
    }

    @Override
    public boolean isCateringIncluded() {
        return true;
    }

    @Override
    public int getPriorityLevel() {
        return 1; // Highest priority
    }
}
