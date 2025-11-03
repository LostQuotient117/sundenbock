package de.nak.iaa.sundenbock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for controller-level exceptions.
 * <p>
 * Translates exceptions into meaningful HTTP responses with structured bodies,
 * including timestamps, status codes and error messages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link ResourceNotFoundException} (HTTP 404).
     * @param ex The caught exception.
     * @return A 404 Not Found response entity.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Not Found",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link BadCredentialsException} (HTTP 401).
     * Thrown by Spring Security on login failure (e.g., wrong password).
     * @param ex The caught exception.
     * @return A 401 Unauthorized response entity.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles {@link DuplicateResourceException} (HTTP 409).
     * Thrown when attempting to create a resource that already exists (e.g., duplicate username).
     * @param ex The caught exception.
     * @return A 409 Conflict response entity.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResourceException(DuplicateResourceException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link UserDisabledException} (HTTP 401).
     * Thrown when a disabled user attempts to authenticate.
     * @param ex The caught exception.
     * @return A 401 Unauthorized response entity with a specific message.
     */
    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<Object> handleUserDisabledException(UserDisabledException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} (HTTP 400).
     * Thrown by the @Valid annotation when DTO validation fails.
     * Collects all field-specific errors into a structured response.
     * @param ex The caught validation exception.
     * @return A 400 Bad Request response entity with detailed field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", "Validation failed",
                "fieldErrors", errors
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

}
