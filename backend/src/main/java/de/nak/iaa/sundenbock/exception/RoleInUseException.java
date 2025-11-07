package de.nak.iaa.sundenbock.exception;

/**
 * Thrown when an attempt is made to delete a Role that is still
 * assigned to one or more Users.
 */
public class RoleInUseException extends RuntimeException {
    public RoleInUseException(String message) {
        super(message);
    }
}
