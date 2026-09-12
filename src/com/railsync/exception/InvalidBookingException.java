package com.railsync.exception;

/**
 * Thrown when booking request violates business constraints (e.g. 0 passengers, invalid journey date).
 */
public class InvalidBookingException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidBookingException(String message) {
        super(message);
    }
}
