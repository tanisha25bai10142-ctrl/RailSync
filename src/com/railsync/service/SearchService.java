package com.railsync.service;

import com.railsync.exception.InvalidStationException;
import com.railsync.model.Booking;
import com.railsync.model.Passenger;
import com.railsync.model.SeatClass;
import com.railsync.model.Station;
import com.railsync.model.Ticket;
import com.railsync.model.Train;
import com.railsync.util.ValidationUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for querying trains, stations, bookings, and passengers.
 * Demonstrates:
 * - Method Overloading
 * - Lambda Expressions
 * - Custom Comparators
 * - Stream API / Collections filtering
 * - String pattern searching
 */
public class SearchService {

    private final Collection<Train> trainCatalog;
    private final Map<String, Booking> bookingMap;
    private final FareCalculator fareCalculator;

    public SearchService(Collection<Train> trainCatalog, Map<String, Booking> bookingMap, FareCalculator fareCalculator) {
        this.trainCatalog = trainCatalog;
        this.bookingMap = bookingMap;
        this.fareCalculator = fareCalculator;
    }

    /**
     * Searches trains between source and destination stations.
     */
    public List<Train> searchTrains(Station source, Station destination, LocalDate journeyDate)
            throws InvalidStationException {
        ValidationUtils.validateStations(source, destination);

        List<Train> results = new ArrayList<>();
        for (Train train : trainCatalog) {
            if (train.servesRoute(source, destination)) {
                results.add(train);
            }
        }
        return results;
    }

    /**
     * Overloaded search filtering by required SeatClass.
     */
    public List<Train> searchTrains(Station source, Station destination, LocalDate journeyDate, SeatClass seatClass)
            throws InvalidStationException {
        List<Train> matched = searchTrains(source, destination, journeyDate);
        if (seatClass == null) {
            return matched;
        }
        return matched.stream()
                .filter(t -> t.getAvailableClasses().contains(seatClass))
                .collect(Collectors.toList());
    }

    /**
     * Search trains by partial number or name query.
     */
    public List<Train> searchTrainsByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(trainCatalog);
        }
        String cleanQuery = query.trim().toLowerCase();
        return trainCatalog.stream()
                .filter(t -> t.getTrainNumber().toLowerCase().contains(cleanQuery) ||
                        t.getTrainName().toLowerCase().contains(cleanQuery) ||
                        t.getSource().getName().toLowerCase().contains(cleanQuery) ||
                        t.getDestination().getName().toLowerCase().contains(cleanQuery))
                .collect(Collectors.toList());
    }

    // ================= Comparators & Sorting =================

    public static Comparator<Train> sortByDepartureTime() {
        return (t1, t2) -> t1.getDepartureTime().compareTo(t2.getDepartureTime());
    }

    public static Comparator<Train> sortByDuration() {
        return (t1, t2) -> t1.getJourneyDuration().compareTo(t2.getJourneyDuration());
    }

    public static Comparator<Train> sortByTrainNumber() {
        return Comparator.comparing(Train::getTrainNumber);
    }

    public static Comparator<Train> sortBySpeedDesc() {
        return (t1, t2) -> Double.compare(t2.getSpeedKmph(), t1.getSpeedKmph());
    }

    public List<Train> sortTrains(List<Train> trains, Comparator<Train> comparator) {
        List<Train> sorted = new ArrayList<>(trains);
        sorted.sort(comparator);
        return sorted;
    }

    // ================= Passenger & Booking Multi-Criteria Search =================

    public Optional<Booking> findBookingByPNR(String pnr) {
        if (pnr == null) return Optional.empty();
        return Optional.ofNullable(bookingMap.get(pnr.trim().toUpperCase()));
    }

    public List<Booking> searchBookingsByPassengerName(String partialName) {
        if (partialName == null || partialName.trim().isEmpty()) return Collections.emptyList();
        String target = partialName.trim().toLowerCase();

        return bookingMap.values().stream()
                .filter(b -> b.getTickets().stream()
                        .anyMatch(t -> t.getPassenger().getName().toLowerCase().contains(target)))
                .collect(Collectors.toList());
    }

    public List<Booking> searchBookingsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return Collections.emptyList();
        String cleanPhone = phone.trim();

        return bookingMap.values().stream()
                .filter(b -> b.getTickets().stream()
                        .anyMatch(t -> t.getPassenger().getPhone().contains(cleanPhone)))
                .collect(Collectors.toList());
    }

    public List<Booking> searchBookingsByTrain(String trainNumber) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) return Collections.emptyList();
        String target = trainNumber.trim();

        return bookingMap.values().stream()
                .filter(b -> b.getTrainNumber().equalsIgnoreCase(target))
                .collect(Collectors.toList());
    }

    public List<Booking> searchBookingsByUser(String userId) {
        if (userId == null) return Collections.emptyList();
        return bookingMap.values().stream()
                .filter(b -> b.getBookedByUserId().equalsIgnoreCase(userId))
                .collect(Collectors.toList());
    }
}
