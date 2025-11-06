package de.nak.iaa.sundenbock.dto.permissionDTO;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) representing a permission.
 * <p>
 * Contains the name of the permission, which must not be blank.
 * </p>
 */
public record PermissionDTO(
        @NotBlank(message = "Permission name must not be empty")
        String name
) {}
