package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.roleDTO.CreateRoleDTO;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO;
import de.nak.iaa.sundenbock.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for managing roles.
 * <p>
 * Exposes endpoints to list roles, create new roles and update a role's permissions.
 */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Retrieves a list of all available roles.
     *
     * @return a list of {@link RoleDTO} representing all roles
     */
    @GetMapping
    public List<RoleDTO> getAllRoles() {
        return roleService.getAllRoles();
    }

    /**
     * Creates a new role.
     *
     * @param createRoleDTO contains the name and permissions for the new role
     * @return the created {@link RoleDTO}
     */
    @PostMapping
    public RoleDTO createRole(@Valid @RequestBody CreateRoleDTO createRoleDTO) {
        return roleService.createRole(createRoleDTO);
    }

    /**
     * Updates the permissions assigned to an existing role.
     *
     * @param roleId          the ID of the role to update
     * @param permissionNames a set of permission names to assign to the role
     */
    @PutMapping("/{roleId}/update-permissions")
    public void updateRolePermissions(@PathVariable Long roleId, @RequestBody Set<String> permissionNames) {
        roleService.updateRolePermissions(roleId, permissionNames);
    }
}
