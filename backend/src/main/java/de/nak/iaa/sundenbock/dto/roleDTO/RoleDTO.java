package de.nak.iaa.sundenbock.dto.roleDTO;

import java.util.Set;

public record RoleDTO(
        Long id,
        String name,
        Set<String> permissions
) {}
