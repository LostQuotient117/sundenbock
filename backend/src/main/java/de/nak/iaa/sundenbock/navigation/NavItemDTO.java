package de.nak.iaa.sundenbock.navigation;

import java.util.Set;

public record NavItemDTO(
        String label,
        String path,
        String icon,
        Set<String> requiredRoles
) {}
