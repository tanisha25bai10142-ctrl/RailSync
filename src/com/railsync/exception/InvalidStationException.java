package com.railsync.exception;

/**
 * Thrown when station inputs are illegal (e.g. source and destination are identical, or station code is unknown).
 */
public class InvalidStationException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidStationException(String message) {
        super(message);
    }
}
