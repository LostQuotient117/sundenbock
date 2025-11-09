package de.nak.iaa.sundenbock.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
     * Handles {@link MethodArgumentNotValidException} (HTTP 400).
     * <p>
     * Thrown by the @Valid annotation when DTO validation fails.
     * Collects all field-specific errors into a structured response.
     *
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

    /**
     * Handles {@link ConstraintViolationException} (HTTP 400).
     * <p>
     * This exception is typically thrown when a validation constraint is violated
     * (e.g., invalid input data). The method extracts all constraint violations,
     * maps them to their respective fields, and constructs a structured response body.
     *
     * @param ex The caught {@link ConstraintViolationException}.
     * @return A {@link ResponseEntity} with HTTP 400 (Bad Request) and a detailed error body.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String fieldName = cv.getPropertyPath().toString();
            if (fieldName.contains(".")) {
                fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
            }
            String errorMessage = cv.getMessage();
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

    /**
     * Handles {@link UserDisabledException} (HTTP 403).
     * <p>
     * Thrown when a user attempts to log in but their account is disabled.
     *
     * @param ex The caught exception.
     * @return A 403 Forbidden response entity.
     */
    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<Object> handleUserDisabledException(UserDisabledException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.FORBIDDEN.value(),
                "error", "Forbidden",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link BadCredentialsException} (HTTP 401).
     * <p>
     * Thrown by Spring Security on login failure (e.g., wrong password).
     *
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

    // TODO implement @PreAuthorize
    /**
     * Handles {@link AccessDeniedException} (HTTP 403).
     * <p>
     * Thrown by @PreAuthorize when a user is authenticated but lacks
     * the required authority (role or permission) to access an endpoint.
     *
     * @return A 403 Forbidden response entity.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException() {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.FORBIDDEN.value(),
                "error", "Forbidden",
                "message", "You do not have permission to perform this action."
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link SelfActionException} (HTTP 409).
     * <p>
     * This exception is thrown when a user attempts to perform an action
     * that is not allowed on their own account or entity.
     * Constructs a structured response body containing a timestamp, HTTP status,
     * error type, and a detailed message from the exception.
     *
     * @param ex The caught {@link SelfActionException}.
     * @return A {@link ResponseEntity} with HTTP 409 (Conflict) and a descriptive error body.
     */
    @ExceptionHandler(SelfActionException.class)
    public ResponseEntity<Object> handleSelfActionException(SelfActionException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link ResourceNotFoundException} (HTTP 404).
     *
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
     * Handles {@link DuplicateResourceException} (HTTP 409).
     * <p>
     * Thrown when attempting to create a resource that already exists (e.g., duplicate username).
     *
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
     * Handles {@link UserInUseException} (HTTP 409).
     * <p>
     * Thrown when attempting to delete a user that is still referenced
     * by tickets or comments.
     *
     * @param ex The caught UserInUseException.
     * @return A 409 Conflict response entity.
     */
    @ExceptionHandler(UserInUseException.class)
    public ResponseEntity<Object> handleUserInUseException(UserInUseException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link RoleInUseException} (HTTP 409).
     * <p>
     * Thrown when attempting to delete a role that is still in use by users.
     *
     * @param ex The caught RoleInUseException.
     * @return A 409 Conflict response entity.
     */
    @ExceptionHandler(RoleInUseException.class)
    public ResponseEntity<Object> handleRoleInUseException(RoleInUseException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link PermissionInUseException} (HTTP 409).
     * <p>
     * Thrown when attempting to delete a permission that is still in use.
     *
     * @param ex The caught PermissionInUseException.
     * @return A 409 Conflict response entity.
     */
    @ExceptionHandler(PermissionInUseException.class)
    public ResponseEntity<Object> handlePermissionInUseException(PermissionInUseException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link DataIntegrityViolationException} (HTTP 409).
     * <p>
     * Typically thrown when a database constraint is violated (e.g. unique constraint, foreign key).
     * Constructs a structured response body containing a timestamp, HTTP status, error type and a
     * detailed message using the exception's most specific cause.
     *
     * @param ex the caught {@link DataIntegrityViolationException}
     * @return a {@link ResponseEntity} with HTTP 409 (Conflict) and a body describing the violation
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", "Database constraint violation: " + ex.getMostSpecificCause().getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link IllegalArgumentException} (HTTP 400).
     * <p>
     * Thrown when a method receives an argument that is invalid,
     * such as a bad enum value or a logically incorrect parameter
     * not caught by DTO validation.
     *
     * @param ex the caught {@link IllegalArgumentException}
     * @return a {@link ResponseEntity} with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", "An invalid argument was provided: " + ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other uncaught {@link Exception}s (HTTP 500).
     * <p>
     * @param ex The caught exception.
     * @return A 500 Internal Server Error response entity.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", "An unexpected error occurred: " + ex.getMessage()
        );
        // TODO Logging Fehler für das Debugging log.error("Unhandled exception: ", ex);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link MismatchedIdException} (HTTP 400).
     * <p>
     * Thrown when the resource identifier in the URL/path does not match the identifier
     * provided in the request body or parameters. This safeguards against accidental or
     * malicious updates by enforcing ID consistency across request components.
     *
     * @param ex the caught {@link MismatchedIdException}
     * @return a {@link ResponseEntity} with HTTP 400 (Bad Request) and a descriptive error body
     */
    @ExceptionHandler(MismatchedIdException.class)
    public ResponseEntity<Object> handleMismatchedIdException(MismatchedIdException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link TicketAlreadyClosedException} (HTTP 409).
     * <p>
     * Thrown when an operation is attempted on a ticket that has already been closed.
     *
     * @param ex The caught exception.
     * @return A 409 Conflict response entity.
     */
    @ExceptionHandler(TicketAlreadyClosedException.class)
    public ResponseEntity<Object> handleTicketAlreadyClosedException(TicketAlreadyClosedException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", System.currentTimeMillis(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Invalid Ticket Status Transition",
                        "message", ex.getMessage()
                ));
    }
}
