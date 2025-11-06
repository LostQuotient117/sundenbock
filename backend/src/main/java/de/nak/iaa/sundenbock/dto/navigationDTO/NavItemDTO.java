package de.nak.iaa.sundenbock.dto.navigationDTO;

import java.util.Set;

/**
 * Data Transfer Object (DTO) representing a navigation item.
 * <p>
 * This record holds information about a navigation item such as its label,
 * path, icon, and the set of permissions required to access it.
 * </p>
 */
public record NavItemDTO(
        String label,
        String path,
        String icon,
        Set<String> requiredPermissions
) {}
