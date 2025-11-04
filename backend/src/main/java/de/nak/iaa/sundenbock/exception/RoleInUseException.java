package de.nak.iaa.sundenbock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an attempt is made to delete a Role that is still
 * assigned to one or more Users.
 */
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict
public class RoleInUseException extends RuntimeException {
    public RoleInUseException(String message) {
        super(message);
    }
}
