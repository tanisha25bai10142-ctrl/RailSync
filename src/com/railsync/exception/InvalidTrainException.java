package com.railsync.exception;

/**
 * Thrown when train configuration or scheduling is invalid.
 */
public class InvalidTrainException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidTrainException(String message) {
        super(message);
    }
}
