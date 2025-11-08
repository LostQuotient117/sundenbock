package de.nak.iaa.sundenbock.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Data Transfer Object (DTO) used for creating a new user.
 * <p>
 * Contains the username, email, password, and a set of roles for the new user.
 * The username must be between 3 and 50 characters, the email must be valid,
 * and the password must be at least 6 characters long.
 * </p>
 */
public record CreateUserDTO(
        @NotBlank(message = "Username must not be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^\\S*$", message = "Username must not contain any whitespace")
        String username,

        @NotBlank(message = "First name must not be empty")
        String firstName,

        @NotBlank(message = "Last name must not be empty")
        String lastName,

        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password must not be empty")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        Set<String> roles
) {}
