package de.nak.iaa.sundenbock.exception;

/**
 * Runtime exception thrown when a requested resource (e.g., a User, Ticket, Role)
 * cannot be found in the database.
 * <p>
 * Mapped to HTTP 404 Not Found by the {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
