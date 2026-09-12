package com.railsync.exception;

/**
 * Root checked exception for all business and domain errors in RailSync.
 * Demonstrates custom exception hierarchies in Core Java.
 */
public class RailSyncException extends Exception {
    private static final long serialVersionUID = 1L;

    public RailSyncException(String message) {
        super(message);
    }

    public RailSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
