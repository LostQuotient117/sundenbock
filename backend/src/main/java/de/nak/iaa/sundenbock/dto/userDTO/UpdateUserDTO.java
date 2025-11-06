package de.nak.iaa.sundenbock.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating a User via PUT.
 * Converted to a record for consistency and immutability.
 */
public record UpdateUserDTO(
        @Email(message = "Email should be valid")
        @Size(max = 255, message = "Email must be less than 255 characters")
        String email,

        Boolean enabled
) {
}