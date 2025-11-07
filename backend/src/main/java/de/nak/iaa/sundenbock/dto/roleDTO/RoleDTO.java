package de.nak.iaa.sundenbock.dto.roleDTO;

import java.util.Set;

/**
 * Data Transfer Object (DTO) representing a role.
 * <p>
 * Contains the role's unique identifier, name, and the set of permissions
 * assigned to the role.
 * </p>
 */
public record RoleDTO(
        Long id,
        String name,
        Set<String> permissions
) {}
