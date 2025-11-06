package de.nak.iaa.sundenbock.dto.navigationDTO;

import java.util.Set;

public record NavItemDTO(
        String label,
        String path,
        String icon,
        Set<String> requiredPermissions
) {}
