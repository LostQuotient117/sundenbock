package de.nak.iaa.sundenbock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an attempt is made to delete a Permission that is still
 * assigned to one or more Roles or Users.
 */
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict
public class PermissionInUseException extends RuntimeException {
    public PermissionInUseException(String message) {
        super(message);
    }
}
