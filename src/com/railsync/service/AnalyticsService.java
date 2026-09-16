package com.railsync.service;

import com.railsync.model.Booking;
import com.railsync.model.BookingStatus;
import com.railsync.model.Coach;
import com.railsync.model.SeatClass;
import com.railsync.model.Ticket;
import com.railsync.model.Train;

import java.util.*;

/**
 * Provides summary statistics and administrative reports from stored trains and bookings.
 */
public class AnalyticsService {

    private final Collection<Train> trains;
    private final Collection<Booking> bookings;

    public AnalyticsService(Collection<Train> trains, Collection<Booking> bookings) {
        this.trains = trains;
        this.bookings = bookings;
    }

    public int getTotalTrains() {
        return trains.size();
    }

    public int getTotalBookings() {
        return bookings.size();
    }

    public int getTotalTicketsBooked() {
        int count = 0;
        for (Booking b : bookings) {
            count += b.getTickets().size();
        }
        return count;
    }

    public int getConfirmedTicketsCount() {
        int count = 0;
        for (Booking b : bookings) {
            for (Ticket t : b.getTickets()) {
                if (!t.isCancelled() && t.getStatus() == BookingStatus.CONFIRMED) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getRacTicketsCount() {
        int count = 0;
        for (Booking b : bookings) {
            for (Ticket t : b.getTickets()) {
                if (!t.isCancelled() && t.getStatus() == BookingStatus.RAC) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getWaitingListTicketsCount() {
        int count = 0;
        for (Booking b : bookings) {
            for (Ticket t : b.getTickets()) {
                if (!t.isCancelled() && t.getStatus() == BookingStatus.WAITING_LIST) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getCancelledTicketsCount() {
        int count = 0;
        for (Booking b : bookings) {
            for (Ticket t : b.getTickets()) {
                if (t.isCancelled() || t.getStatus() == BookingStatus.CANCELLED) {
                    count++;
                }
            }
        }
        return count;
    }

    public double getTotalGrossRevenue() {
        double revenue = 0.0;
        for (Booking b : bookings) {
            revenue += b.getTotalFare();
        }
        return revenue;
    }

    public double getTotalActiveRevenue() {
        double revenue = 0.0;
        for (Booking b : bookings) {
            for (Ticket t : b.getTickets()) {
                if (!t.isCancelled()) {
                    revenue += t.getIndividualFare();
                }
            }
        }
        return revenue;
    }

    public String getMostOccupiedTrain() {
        if (trains.isEmpty()) return "N/A";

        Map<String, Integer> trainOccupancy = new HashMap<>();
        for (Booking b : bookings) {
            for (Ticket t : b.getTickets()) {
                if (!t.isCancelled()) {
                    trainOccupancy.put(b.getTrainNumber(),
                            trainOccupancy.getOrDefault(b.getTrainNumber(), 0) + 1);
                }
            }
        }

        String maxTrainNo = null;
        int maxCount = -1;
        for (Map.Entry<String, Integer> entry : trainOccupancy.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxTrainNo = entry.getKey();
            }
        }

        if (maxTrainNo == null) {
            // Default to first train if no bookings yet
            Train first = trains.iterator().next();
            return first.getTrainNumber() + " - " + first.getTrainName() + " (0 seats)";
        }

        for (Train t : trains) {
            if (t.getTrainNumber().equalsIgnoreCase(maxTrainNo)) {
                return t.getTrainNumber() + " - " + t.getTrainName() + " (" + maxCount + " booked)";
            }
        }
        return maxTrainNo + " (" + maxCount + " booked)";
    }

    public String getMostPopularRoute() {
        if (bookings.isEmpty()) return "N/A";

        Map<String, Integer> routeCounts = new HashMap<>();
        for (Booking b : bookings) {
            String routeKey = b.getSource().getCode() + " ➔ " + b.getDestination().getCode();
            routeCounts.put(routeKey, routeCounts.getOrDefault(routeKey, 0) + 1);
        }

        String topRoute = "N/A";
        int max = 0;
        for (Map.Entry<String, Integer> entry : routeCounts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                topRoute = entry.getKey();
            }
        }
        return topRoute + " (" + max + " bookings)";
    }

    public Map<SeatClass, Integer> getClassWiseOccupancy() {
        Map<SeatClass, Integer> map = new EnumMap<>(SeatClass.class);
        for (SeatClass sc : SeatClass.values()) {
            map.put(sc, 0);
        }
        for (Booking b : bookings) {
            for (Ticket t : b.getTickets()) {
                if (!t.isCancelled()) {
                    SeatClass sc = t.getSeatClass();
                    map.put(sc, map.getOrDefault(sc, 0) + 1);
                }
            }
        }
        return map;
    }

    public double getAverageOccupancyPercentage() {
        int totalPhysicalCapacity = 0;
        for (Train train : trains) {
            for (Coach coach : train.getCoaches()) {
                totalPhysicalCapacity += coach.getTotalConfirmedCapacity();
            }
        }
        if (totalPhysicalCapacity == 0) return 0.0;
        int activeConfirmed = getConfirmedTicketsCount();
        return Math.min(100.0, (double) activeConfirmed / totalPhysicalCapacity * 100.0);
    }
}
