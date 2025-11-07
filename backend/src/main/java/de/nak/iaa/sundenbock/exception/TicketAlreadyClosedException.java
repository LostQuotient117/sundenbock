package de.nak.iaa.sundenbock.exception;

/**
 * Thrown when an operation is attempted on a ticket that is already closed.
 */
public class TicketAlreadyClosedException extends RuntimeException {
    public TicketAlreadyClosedException(String message) {
        super(message);
    }
}
