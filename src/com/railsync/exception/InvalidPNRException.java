package com.railsync.exception;

/**
 * Thrown when an invalid or nonexistent PNR is provided for enquiry or cancellation.
 */
public class InvalidPNRException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidPNRException(String message) {
        super(message);
    }
}
