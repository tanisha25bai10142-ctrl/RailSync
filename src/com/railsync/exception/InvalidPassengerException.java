package com.railsync.exception;

/**
 * Thrown when passenger information fails validation rules (e.g. invalid age or blank name).
 */
public class InvalidPassengerException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidPassengerException(String message) {
        super(message);
    }
}
