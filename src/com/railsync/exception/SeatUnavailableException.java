package com.railsync.exception;

/**
 * Thrown when confirmed, RAC, and waiting list capacities are all completely exhausted.
 */
public class SeatUnavailableException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public SeatUnavailableException(String message) {
        super(message);
    }
}
