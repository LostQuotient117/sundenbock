package de.nak.iaa.sundenbock.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) used for resetting an administrator's password.
 * <p>
 * Contains the new password for the admin, which must not be blank and
 * must have a minimum length of 6 characters.
 * </p>
 */
public record AdminResetPasswordDTO(
        @NotBlank(message = "New password must not be empty")
        @Size(min = 6, message = "New password must be at least 6 characters long")
        String newPassword
) {}
