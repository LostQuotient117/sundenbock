package de.nak.iaa.sundenbock.dto.roleDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CreateRoleDTO(

        @NotBlank(message = "Role name must not be empty")
        String name,

        @NotNull(message = "Permissions set cannot be null")
        Set<String> permissions
) {}
