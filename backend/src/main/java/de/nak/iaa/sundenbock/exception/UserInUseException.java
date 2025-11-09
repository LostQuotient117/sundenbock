package de.nak.iaa.sundenbock.exception;

/**
 * Thrown when an attempt is made to delete a User that is still
 * referenced by other entities (e.g., as a ticket's responsible person or creator).
 */
public class UserInUseException extends RuntimeException {
    public UserInUseException(String message) {
        super(message);
    }
}
