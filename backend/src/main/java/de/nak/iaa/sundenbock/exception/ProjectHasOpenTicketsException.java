package de.nak.iaa.sundenbock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProjectHasOpenTicketsException extends RuntimeException {
    public ProjectHasOpenTicketsException(String message) {
        super(message);
    }
}
