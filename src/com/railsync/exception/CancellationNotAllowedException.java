package com.railsync.exception;

/**
 * Thrown when ticket cancellation violates railway policy (e.g. already cancelled or past departure).
 */
public class CancellationNotAllowedException extends RailSyncException {
    private static final long serialVersionUID = 1L;

    public CancellationNotAllowedException(String message) {
        super(message);
    }
}
