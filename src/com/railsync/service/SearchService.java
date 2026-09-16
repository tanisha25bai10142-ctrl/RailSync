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

/**
 * Service for searching trains, bookings, and passengers.
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
     * Searches trains between source and destination stations with a specific seat class.
     */
    public List<Train> searchTrains(Station source, Station destination, LocalDate journeyDate, SeatClass seatClass)
            throws InvalidStationException {
        List<Train> matched = searchTrains(source, destination, journeyDate);
        if (seatClass == null) {
            return matched;
        }
        List<Train> filtered = new ArrayList<>();
        for (Train train : matched) {
            if (train.getAvailableClasses().contains(seatClass)) {
                filtered.add(train);
            }
        }
        return filtered;
    }

    /**
     * Search trains by partial number, name, or station name.
     */
    public List<Train> searchTrainsByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(trainCatalog);
        }
        String cleanQuery = query.trim().toLowerCase();
        List<Train> results = new ArrayList<>();
        for (Train train : trainCatalog) {
            if (train.getTrainNumber().toLowerCase().contains(cleanQuery) ||
                train.getTrainName().toLowerCase().contains(cleanQuery) ||
                train.getSource().getName().toLowerCase().contains(cleanQuery) ||
                train.getDestination().getName().toLowerCase().contains(cleanQuery)) {
                results.add(train);
            }
        }
        return results;
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

    // ================= Passenger & Booking Search =================

    public Optional<Booking> findBookingByPNR(String pnr) {
        if (pnr == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(bookingMap.get(pnr.trim().toUpperCase()));
    }

    public List<Booking> searchBookingsByPassengerName(String partialName) {
        if (partialName == null || partialName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String target = partialName.trim().toLowerCase();
        List<Booking> results = new ArrayList<>();
        for (Booking b : bookingMap.values()) {
            for (Ticket t : b.getTickets()) {
                if (t.getPassenger().getName().toLowerCase().contains(target)) {
                    results.add(b);
                    break;
                }
            }
        }
        return results;
    }

    public List<Booking> searchBookingsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String cleanPhone = phone.trim();
        List<Booking> results = new ArrayList<>();
        for (Booking b : bookingMap.values()) {
            for (Ticket t : b.getTickets()) {
                if (t.getPassenger().getPhone().contains(cleanPhone)) {
                    results.add(b);
                    break;
                }
            }
        }
        return results;
    }

    public List<Booking> searchBookingsByTrain(String trainNumber) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String target = trainNumber.trim();
        List<Booking> results = new ArrayList<>();
        for (Booking b : bookingMap.values()) {
            if (b.getTrainNumber().equalsIgnoreCase(target)) {
                results.add(b);
            }
        }
        return results;
    }

    public List<Booking> searchBookingsByUser(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Booking> results = new ArrayList<>();
        for (Booking b : bookingMap.values()) {
            if (b.getBookedByUserId().equalsIgnoreCase(userId)) {
                results.add(b);
            }
        }
        return results;
    }
}
