package com.railsync.model;

import java.time.LocalTime;

/**
 * Standard Mail / Express train with conventional stops.
 */
public class ExpressTrain extends Train {
    private static final long serialVersionUID = 1L;

    public ExpressTrain(String trainNumber, String trainName, Station source, Station destination,
                        LocalTime departureTime, LocalTime arrivalTime, double distanceKm) {
        super(trainNumber, trainName, source, destination, departureTime, arrivalTime, distanceKm, 0.50);
    }

    @Override
    public String getTrainType() {
        return "Mail/Express";
    }

    @Override
    public double getSurcharge() {
        return 0.0;
    }

    @Override
    public double getSpeedKmph() {
        return 75.0;
    }

    @Override
    public boolean isCateringIncluded() {
        return false;
    }

    @Override
    public int getPriorityLevel() {
        return 4;
    }
}
