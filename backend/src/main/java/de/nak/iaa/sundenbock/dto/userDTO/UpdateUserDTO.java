package de.nak.iaa.sundenbock.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) used for updating a user's information.
 * <p>
 * Contains the user's email and enabled status. The email must be valid
 * and no longer than 255 characters.
 * </p>
 */
public record UpdateUserDTO(
        @Email(message = "Email should be valid")
        @Size(max = 255, message = "Email must be less than 255 characters")
        String email,

        Boolean enabled
) {
}