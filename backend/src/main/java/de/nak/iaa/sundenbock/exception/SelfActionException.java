package de.nak.iaa.sundenbock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user attempts to perform a restrictive action
 * on their own account (e.g., deleting self, removing own admin rights).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfActionException extends RuntimeException {
    public SelfActionException(String message) {
        super(message);
    }
}
