package de.nak.iaa.sundenbock.exception;

/**
 * Runtime exception thrown when an authentication attempt is made by a disabled user.
 *
 * Mapped to HTTP 401 Unauthorized.
 */
public class UserDisabledException extends RuntimeException {
    public UserDisabledException(String message) {
        super(message);
    }
}
