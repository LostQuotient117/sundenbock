package de.nak.iaa.sundenbock.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) representing an authentication request.
 * <p>
 * Contains the username and password provided by a user during login.
 * Both fields must not be blank.
 * </p>
 */
public record AuthenticationRequest(

        @NotBlank(message = "Username must not be empty")
        String username,

        @NotBlank(message = "Password must not be empty")
        String password
) {}
