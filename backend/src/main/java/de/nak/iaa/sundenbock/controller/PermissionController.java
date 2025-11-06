package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.service.PermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing permissions.
 * <p>
 * Exposes endpoints to list all permissions and to create new permissions.
 */
@NavItem(label = "Permissions", path = "/permissions", icon = "permission")
@RestController
@RequestMapping("/api/v1/permissions")
@Validated
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Retrieves a list of all available permissions.
     *
     * @return a list of {@link PermissionDTO} representing all permissions
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public List<PermissionDTO> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    /**
     * Creates a new permission.
     *
     * @param permissionDTO the permission DTO containing required data
     * @return the created {@link PermissionDTO}
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public PermissionDTO createPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        return permissionService.createPermission(permissionDTO);
    }

    /**
     * Deletes a permission by its name (ID).
     * The permission will only be deleted if it is not currently assigned
     * to any roles or users.
     *
     * @param permissionName The name of the permission to delete.
     */
    @DeleteMapping("/{permissionName}/delete")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public void deletePermission(
            @PathVariable @NotBlank String permissionName) {
        permissionService.deletePermission(permissionName);
    }
}
