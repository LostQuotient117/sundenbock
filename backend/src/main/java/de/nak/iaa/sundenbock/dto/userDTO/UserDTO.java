package de.nak.iaa.sundenbock.dto.userDTO;

/**
 * Data Transfer Object (DTO) representing a basic user.
 * <p>
 * Contains the user's unique identifier and username.
 * </p>
 */
public record UserDTO(
        Long id,
        String username
) { }