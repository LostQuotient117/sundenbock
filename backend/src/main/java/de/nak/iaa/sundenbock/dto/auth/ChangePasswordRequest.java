package de.nak.iaa.sundenbock.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) used for changing a user's password.
 * <p>
 * Contains the old password and the new password. The new password must
 * not be blank and must be at least 6 characters long.
 * </p>
 */
public record ChangePasswordRequest(

        @NotBlank(message = "Old password must not be empty")
        String oldPassword,

        @NotBlank(message = "New password must not be empty")
        @Size(min = 6, message = "New password must be at least 6 characters long")
        String newPassword
) {
}
