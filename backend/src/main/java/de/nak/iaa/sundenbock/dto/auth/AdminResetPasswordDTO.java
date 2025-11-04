package de.nak.iaa.sundenbock.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordDTO(
        @NotBlank(message = "New password must not be empty")
        @Size(min = 6, message = "New password must be at least 6 characters long")
        String newPassword
) {}
