package de.nak.iaa.sundenbock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Runtime exception thrown when an attempt is made to create a resource
 * that violates a unique constraint (e.g., creating a user with an
 * already existing username or email).
 * <p>
 * Mapped to HTTP 409 Conflict by the {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
