package com.railsync.exception;

/**
 * Thrown when an identical booking attempt is detected or duplicate PNR collision occurs.
 */
public class DuplicateBookingException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public DuplicateBookingException(String message) {
        super(message);
    }
}
