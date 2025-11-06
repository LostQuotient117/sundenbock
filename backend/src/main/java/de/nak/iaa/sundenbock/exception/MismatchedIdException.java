package de.nak.iaa.sundenbock.exception;

/**
 * Exception indicating a mismatch between the resource identifier specified in the
 * request path (URL) and the identifier provided in the request body or parameters.
 * <p>
 * This unchecked exception can be thrown in controllers or services to enforce
 * ID consistency across request components and to prevent accidental or malicious
 * updates to the wrong resource.
 * <p>
 * Example: calling <code>PUT /api/tickets/42</code> with a request body containing
 * <code>{ "id": 7, ... }</code>.
 */
public class MismatchedIdException extends RuntimeException {
    public MismatchedIdException(String message) {
        super(message);
    }
}
