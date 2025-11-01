package de.nak.iaa.sundenbock.dto.roleDTO;

import java.util.Set;

public record CreateRoleDTO(
        String name,
        Set<String> permissions
) {}
