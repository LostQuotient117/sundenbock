package de.nak.iaa.sundenbock.dto.auth;

public record AuthenticationRequest(
        String username,
        String password
) {}
