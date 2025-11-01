package de.nak.iaa.sundenbock.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(

        @NotBlank(message = "Username must not be empty")
        String username,

        @NotBlank(message = "Password must not be empty")
        String password
) {}
