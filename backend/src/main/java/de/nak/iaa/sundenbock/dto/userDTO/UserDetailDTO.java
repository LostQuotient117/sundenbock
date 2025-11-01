package de.nak.iaa.sundenbock.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDetailDTO(
        Long id,
        String username,

        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email should be valid")
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean enabled,

        @NotNull(message = "Roles set cannot be null")
        Set<String> roles,

        @NotNull(message = "Permissions set cannot be null")
        Set<String> permissions
) {
}
