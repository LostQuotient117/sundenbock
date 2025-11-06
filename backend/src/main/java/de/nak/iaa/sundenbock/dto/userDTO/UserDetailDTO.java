package de.nak.iaa.sundenbock.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data Transfer Object (DTO) representing detailed information about a user.
 * <p>
 * Contains the user's ID, username, email, creation and update timestamps,
 * enabled status, and sets of roles and permissions. Email must be valid
 * and not blank, while roles and permissions sets cannot be null.
 * </p>
 */
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
