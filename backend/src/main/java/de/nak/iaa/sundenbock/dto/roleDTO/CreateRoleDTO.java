package de.nak.iaa.sundenbock.dto.roleDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * Data Transfer Object (DTO) used for creating a new role.
 * <p>
 * Contains the role's name and a set of permissions associated with the role.
 * The name must not be blank, and the permissions set cannot be null.
 * </p>
 */
public record CreateRoleDTO(

        @NotBlank(message = "Role name must not be empty")
        String name,

        @NotNull(message = "Permissions set cannot be null")
        Set<String> permissions
) {}
