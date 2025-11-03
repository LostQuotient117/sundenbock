package de.nak.iaa.sundenbock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Runtime exception thrown when a requested resource (e.g., a User, Ticket, Role)
 * cannot be found in the database.
 * <p>
 * Mapped to HTTP 404 Not Found by the {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
