package de.nak.iaa.sundenbock.exception;

/**
 * Thrown when a user attempts to perform a restrictive action
 * on their own account (e.g., deleting self, removing own admin rights).
 */
public class SelfActionException extends RuntimeException {
    public SelfActionException(String message) {
        super(message);
    }
}
