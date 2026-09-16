package com.railsync.exception;

/**
 * Base checked exception for RailSync reservation errors.
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
