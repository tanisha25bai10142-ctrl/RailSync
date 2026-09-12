package com.railsync.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Railway coach containing structured seats and managing local capacity.
 * Demonstrates Collections (ArrayList), encapsulation, and composition.
 */
public class Coach implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String coachId;
    private final SeatClass seatClass;
    private final List<Seat> seats;
    private final int racCapacity;
    private final int waitingListCapacity;

    public Coach(String coachId, SeatClass seatClass, int totalSeats, int racCapacity, int waitingListCapacity) {
        this.coachId = Objects.requireNonNull(coachId, "Coach ID cannot be null");
        this.seatClass = Objects.requireNonNull(seatClass, "SeatClass cannot be null");
        this.racCapacity = Math.max(0, racCapacity);
        this.waitingListCapacity = Math.max(0, waitingListCapacity);
        this.seats = new ArrayList<>(totalSeats);
        initializeSeats(totalSeats);
    }

    private void initializeSeats(int totalSeats) {
        for (int i = 1; i <= totalSeats; i++) {
            BerthType berthType = determineBerthType(i, seatClass);
            seats.add(new Seat(i, coachId, seatClass, berthType));
        }
    }

    private BerthType determineBerthType(int seatNum, SeatClass sClass) {
        switch (sClass) {
            case CHAIR_CAR:
                int remCC = seatNum % 5;
                if (remCC == 1 || remCC == 0) return BerthType.WINDOW;
                if (remCC == 2 || remCC == 4) return BerthType.MIDDLE;
                return BerthType.AISLE;

            case SECOND_AC:
                int rem2A = seatNum % 6;
                if (rem2A == 1 || rem2A == 3) return BerthType.LOWER;
                if (rem2A == 2 || rem2A == 4) return BerthType.UPPER;
                if (rem2A == 5) return BerthType.SIDE_LOWER;
                return BerthType.SIDE_UPPER;

            case FIRST_AC:
                return (seatNum % 2 == 1) ? BerthType.LOWER : BerthType.UPPER;

            case THIRD_AC:
            case SLEEPER:
            default:
                int rem = seatNum % 8;
                if (rem == 1 || rem == 4) return BerthType.LOWER;
                if (rem == 2 || rem == 5) return BerthType.MIDDLE;
                if (rem == 3 || rem == 6) return BerthType.UPPER;
                if (rem == 7) return BerthType.SIDE_LOWER;
                return BerthType.SIDE_UPPER;
        }
    }

    public String getCoachId() {
        return coachId;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public int getTotalConfirmedCapacity() {
        return seats.size();
    }

    public int getRacCapacity() {
        return racCapacity;
    }

    public int getWaitingListCapacity() {
        return waitingListCapacity;
    }

    public synchronized int getAvailableConfirmedCount() {
        int count = 0;
        for (Seat s : seats) {
            if (!s.isBooked()) {
                count++;
            }
        }
        return count;
    }

    public synchronized Seat findNextAvailableSeat() {
        for (Seat s : seats) {
            if (!s.isBooked()) {
                return s;
            }
        }
        return null;
    }

    public synchronized boolean releaseSeat(Seat seat) {
        if (seat == null) return false;
        for (Seat s : seats) {
            if (s.equals(seat)) {
                s.setBooked(false);
                s.setRacOccupied(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return coachId + " (" + seatClass.getCode() + ", Seats: " + seats.size() + ")";
    }
}
