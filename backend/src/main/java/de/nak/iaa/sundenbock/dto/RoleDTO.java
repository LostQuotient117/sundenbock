package de.nak.iaa.sundenbock.dto;

import java.util.Set;

public record RoleDTO(
        Long id,
        String name,
        Set<String> permissions
) {}
