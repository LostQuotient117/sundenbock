package de.nak.iaa.sundenbock.dto.permissionDTO;

import jakarta.validation.constraints.NotBlank;

public record PermissionDTO(
        @NotBlank(message = "Permission name must not be empty")
        String name
) {}
