package com.railsync.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

/**
 * Abstract base class representing a train in the reservation system.
 */
public abstract class Train implements Serializable, Comparable<Train> {
    private static final long serialVersionUID = 1L;

    private final String trainNumber;
    private final String trainName;
    private final Station source;
    private final Station destination;
    private final LocalTime departureTime;
    private final LocalTime arrivalTime;
    private final double distanceKm;
    private final List<Coach> coaches;
    private final List<Station> intermediateStations;
    private double baseFareRatePerKm;

    public Train(String trainNumber, String trainName, Station source, Station destination,
                 LocalTime departureTime, LocalTime arrivalTime, double distanceKm,
                 double baseFareRatePerKm) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Train number cannot be null or empty");
        }
        if (trainName == null || trainName.trim().isEmpty()) {
            throw new IllegalArgumentException("Train name cannot be null or empty");
        }
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Source and Destination stations must not be null");
        }
        if (source.equals(destination)) {
            throw new IllegalArgumentException("Source and Destination cannot be the same station");
        }
        this.trainNumber = trainNumber.trim();
        this.trainName = trainName.trim();
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.distanceKm = Math.max(1.0, distanceKm);
        this.baseFareRatePerKm = Math.max(0.2, baseFareRatePerKm);
        this.coaches = new ArrayList<>();
        this.intermediateStations = new ArrayList<>();
    }

    // Abstract methods implemented by specific train types
    public abstract String getTrainType();
    public abstract double getSurcharge();
    public abstract double getSpeedKmph();
    public abstract boolean isCateringIncluded();
    public abstract int getPriorityLevel();

    // ================= Concrete Operations =================
    public void addCoach(Coach coach) {
        if (coach != null) {
            coaches.add(coach);
        }
    }

    public void addIntermediateStation(Station station) {
        if (station != null && !intermediateStations.contains(station)) {
            intermediateStations.add(station);
        }
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public Station getSource() {
        return source;
    }

    public Station getDestination() {
        return destination;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getBaseFareRatePerKm() {
        return baseFareRatePerKm;
    }

    public void setBaseFareRatePerKm(double baseFareRatePerKm) {
        this.baseFareRatePerKm = baseFareRatePerKm;
    }

    public List<Coach> getCoaches() {
        return Collections.unmodifiableList(coaches);
    }

    public List<Station> getIntermediateStations() {
        return Collections.unmodifiableList(intermediateStations);
    }

    /**
     * Checks if train serves both source and destination in route direction.
     */
    public boolean servesRoute(Station from, Station to) {
        if (from == null || to == null) return false;
        List<Station> fullRoute = getFullRouteStations();
        int fromIdx = fullRoute.indexOf(from);
        int toIdx = fullRoute.indexOf(to);
        return fromIdx != -1 && toIdx != -1 && fromIdx < toIdx;
    }

    public List<Station> getFullRouteStations() {
        List<Station> route = new ArrayList<>();
        route.add(source);
        route.addAll(intermediateStations);
        route.add(destination);
        return route;
    }

    public Set<SeatClass> getAvailableClasses() {
        Set<SeatClass> classes = new LinkedHashSet<>();
        for (Coach c : coaches) {
            classes.add(c.getSeatClass());
        }
        return classes;
    }

    public List<Coach> getCoachesForClass(SeatClass seatClass) {
        List<Coach> matched = new ArrayList<>();
        for (Coach c : coaches) {
            if (c.getSeatClass() == seatClass) {
                matched.add(c);
            }
        }
        return matched;
    }

    public synchronized int getConfirmedCapacity(SeatClass seatClass) {
        int total = 0;
        for (Coach c : coaches) {
            if (c.getSeatClass() == seatClass) {
                total += c.getTotalConfirmedCapacity();
            }
        }
        return total;
    }

    public synchronized int getAvailableConfirmedSeats(SeatClass seatClass) {
        int total = 0;
        for (Coach c : coaches) {
            if (c.getSeatClass() == seatClass) {
                total += c.getAvailableConfirmedCount();
            }
        }
        return total;
    }

    public synchronized int getRacCapacity(SeatClass seatClass) {
        int total = 0;
        for (Coach c : coaches) {
            if (c.getSeatClass() == seatClass) {
                total += c.getRacCapacity();
            }
        }
        return total;
    }

    public synchronized int getWaitingListCapacity(SeatClass seatClass) {
        int total = 0;
        for (Coach c : coaches) {
            if (c.getSeatClass() == seatClass) {
                total += c.getWaitingListCapacity();
            }
        }
        return total;
    }

    public Duration getJourneyDuration() {
        long depSeconds = departureTime.toSecondOfDay();
        long arrSeconds = arrivalTime.toSecondOfDay();
        if (arrSeconds < depSeconds) {
            arrSeconds += 24 * 3600; // overnight journey
        }
        return Duration.ofSeconds(arrSeconds - depSeconds);
    }

    public String getFormattedDuration() {
        Duration dur = getJourneyDuration();
        long hours = dur.toHours();
        long mins = dur.toMinutesPart();
        return String.format("%02dh %02dm", hours, mins);
    }

    @Override
    public int compareTo(Train o) {
        if (o == null) return 1;
        return this.trainNumber.compareTo(o.trainNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Train)) return false;
        Train train = (Train) o;
        return Objects.equals(trainNumber, train.trainNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainNumber);
    }

    @Override
    public String toString() {
        return trainNumber + " - " + trainName + " [" + getTrainType() + "] (" +
                source.getCode() + " -> " + destination.getCode() + ")";
    }
}
