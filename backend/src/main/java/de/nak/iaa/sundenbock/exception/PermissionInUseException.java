package de.nak.iaa.sundenbock.exception;

/**
 * Thrown when an attempt is made to delete a Permission that is still
 * assigned to one or more Roles or Users.
 */
public class PermissionInUseException extends RuntimeException {
    public PermissionInUseException(String message) {
        super(message);
    }
}
