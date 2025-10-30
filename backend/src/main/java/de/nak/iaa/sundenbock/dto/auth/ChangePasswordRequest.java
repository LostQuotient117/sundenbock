package de.nak.iaa.sundenbock.dto.auth;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {
}
