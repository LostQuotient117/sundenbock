package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing permissions.
 * <p>
 * Exposes endpoints to list all permissions and to create new permissions.
 */
@RestController
@RequestMapping("/api/v1/permissions")
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
    public PermissionDTO createPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        return permissionService.createPermission(permissionDTO);
    }
}
