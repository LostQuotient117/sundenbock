package de.nak.iaa.sundenbock.dto.auth;

/**
 * Data Transfer Object (DTO) representing an authentication response.
 * <p>
 * Contains the token returned to the user upon successful authentication.
 * </p>
 */
public record AuthenticationResponse(
        String token
) {}
