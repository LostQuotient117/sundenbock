package de.nak.iaa.sundenbock.dto.auth;

public record RegistrationRequest(
        String username,
        String email,
        String password
) {}
