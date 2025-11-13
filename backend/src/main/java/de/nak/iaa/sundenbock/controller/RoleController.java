package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.roleDTO.CreateRoleDTO;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO;
import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleWithUsersDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.service.RoleService;
import de.nak.iaa.sundenbock.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for managing roles.
 * <p>
 * Exposes endpoints to list roles, create new roles and update a role's permissions.
 */
@NavItem(label = "Roles", path = "/roles", icon = "role")
@RestController
@RequestMapping("/api/v1/roles")
@Validated
public class RoleController {

    private final RoleService roleService;
    private final UserService userService;

    public RoleController(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    /**
     * Retrieves a list of all available roles.
     *
     * @return a list of {@link RoleDTO} representing all roles
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public List<RoleDTO> getAllRoles() {
        return roleService.getAllRoles();
    }

    /**
     * Retrieves a list of all users assigned to a specific role.
     *
     * @param roleName The name of the role.
     * @return a list of {@link UserDTO} representing the users.
     */
    @GetMapping("/{roleName}/users")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public List<UserDTO> getUsersByRole(@PathVariable @NotBlank String roleName) {
        return userService.getUsersByRole(roleName);
    }

    /**
     * Retrieves all roles, each populated with a list of users assigned to it.
     *
     * @return a list of {@link RoleWithUsersDTO}
     */
    @GetMapping("/with-users")
    @PreAuthorize("hasAuthority('ROLE_MANAGE') or hasAuthority('USER_MANAGE')")
    public List<RoleWithUsersDTO> getAllRolesWithUsers() {
        return roleService.getAllRolesWithUsers();
    }

    /**
     * Creates a new role.
     *
     * @param createRoleDTO contains the name and permissions for the new role
     * @return the created {@link RoleDTO}
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
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
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public void updateRolePermissions(@PathVariable @Min(1) Long roleId,
                                      @RequestBody Set<String> permissionNames) {
        roleService.updateRolePermissions(roleId, permissionNames);
    }

    /**
     * Deletes a role by its ID.
     * The role will only be deleted if it is not a core role
     * and not currently assigned to any users.
     *
     * @param roleId The ID of the role to delete.
     */
    @DeleteMapping("/{roleId}/delete")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public void deleteRole(@PathVariable @Min(1) Long roleId) {
        roleService.deleteRole(roleId);
    }
}
