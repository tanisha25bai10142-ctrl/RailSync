package com.railsync.exception;

/**
 * Thrown when a queried train number is not present in the catalog.
 */
public class TrainNotFoundException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public TrainNotFoundException(String message) {
        super(message);
    }
}
