package com.railsync.model;

import java.io.Serializable;

/**
 * Enumerates possible statuses for tickets and bookings in the reservation lifecycle.
 */
public enum BookingStatus implements Serializable {
    CONFIRMED("Confirmed (CNF)"),
    RAC("Reservation Against Cancellation (RAC)"),
    WAITING_LIST("Waiting List (WL)"),
    CANCELLED("Cancelled (CAN)");

    private final String displayStatus;

    BookingStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }

    @Override
    public String toString() {
        return displayStatus;
    }
}
