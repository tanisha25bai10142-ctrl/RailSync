package com.railsync.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a complete booking transaction containing one or multiple passenger tickets.
 * Demonstrates composition, Collections (ArrayList), and Java Time API (LocalDate, LocalDateTime).
 */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String bookingId;
    private final String pnr;
    private final String trainNumber;
    private final String trainName;
    private final Station source;
    private final Station destination;
    private final LocalDate journeyDate;
    private final LocalDateTime bookingTimestamp;
    private final SeatClass seatClass;
    private final List<Ticket> tickets;
    private double totalFare;
    private boolean cancelled;
    private final String bookedByUserId;

    public Booking(String bookingId, String pnr, String trainNumber, String trainName,
                   Station source, Station destination, LocalDate journeyDate,
                   SeatClass seatClass, String bookedByUserId) {
        this.bookingId = Objects.requireNonNull(bookingId, "Booking ID cannot be null");
        this.pnr = Objects.requireNonNull(pnr, "PNR cannot be null");
        this.trainNumber = Objects.requireNonNull(trainNumber, "Train number cannot be null");
        this.trainName = Objects.requireNonNull(trainName, "Train name cannot be null");
        this.source = Objects.requireNonNull(source, "Source station cannot be null");
        this.destination = Objects.requireNonNull(destination, "Destination station cannot be null");
        this.journeyDate = Objects.requireNonNull(journeyDate, "Journey date cannot be null");
        this.seatClass = Objects.requireNonNull(seatClass, "SeatClass cannot be null");
        this.bookedByUserId = bookedByUserId != null ? bookedByUserId : "GUEST";
        this.bookingTimestamp = LocalDateTime.now();
        this.tickets = new ArrayList<>();
        this.totalFare = 0.0;
        this.cancelled = false;
    }

    public void addTicket(Ticket ticket) {
        if (ticket != null) {
            tickets.add(ticket);
            recalculateTotalFare();
        }
    }

    public void recalculateTotalFare() {
        double sum = 0.0;
        for (Ticket t : tickets) {
            sum += t.getIndividualFare();
        }
        this.totalFare = sum;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getPnr() {
        return pnr;
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

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public LocalDateTime getBookingTimestamp() {
        return bookingTimestamp;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public double getTotalFare() {
        return totalFare;
    }

    public boolean isCancelled() {
        if (cancelled) return true;
        // If all tickets in booking are cancelled, the whole booking is cancelled
        if (tickets.isEmpty()) return false;
        for (Ticket t : tickets) {
            if (!t.isCancelled()) return false;
        }
        return true;
    }

    public boolean isAllCancelled() {
        return isCancelled();
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (cancelled) {
            for (Ticket t : tickets) {
                t.setCancelled(true);
            }
        }
    }

    public String getBookedByUserId() {
        return bookedByUserId;
    }

    public int getPassengerCount() {
        return tickets.size();
    }

    public int getConfirmedCount() {
        int c = 0;
        for (Ticket t : tickets) {
            if (!t.isCancelled() && t.getStatus() == BookingStatus.CONFIRMED) c++;
        }
        return c;
    }

    public int getRacCount() {
        int c = 0;
        for (Ticket t : tickets) {
            if (!t.isCancelled() && t.getStatus() == BookingStatus.RAC) c++;
        }
        return c;
    }

    public int getWaitingListCount() {
        int c = 0;
        for (Ticket t : tickets) {
            if (!t.isCancelled() && t.getStatus() == BookingStatus.WAITING_LIST) c++;
        }
        return c;
    }

    public String getFormattedBookingTime() {
        return bookingTimestamp.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
    }

    public String getFormattedJourneyDate() {
        return journeyDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy (EEE)"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(pnr, booking.pnr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pnr);
    }

    @Override
    public String toString() {
        return "Booking[PNR=" + pnr + ", Train=" + trainNumber + ", Passengers=" + tickets.size() +
                ", Date=" + journeyDate + ", Total=₹" + String.format("%.2f", totalFare) + "]";
    }
}
