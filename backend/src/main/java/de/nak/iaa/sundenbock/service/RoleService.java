package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.roleDTO.CreateRoleDTO;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO;
import de.nak.iaa.sundenbock.dto.mapper.RoleMapper;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleWithUsersDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.RoleInUseException;
import de.nak.iaa.sundenbock.exception.SelfActionException;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import de.nak.iaa.sundenbock.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service handling role-related operations.
 * <p>
 * Provides methods to list roles, create a role with a set of permissions,
 * and update the permissions assigned to an existing role.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;
    private final UserService userService;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, RoleMapper roleMapper, UserRepository userRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Retrieves all existing roles as a list of {@link RoleDTO}.
     *
     * @return a list of all roles
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all roles, each populated with a list of users assigned to it.
     *
     * @return A list of {@link RoleWithUsersDTO}.
     */
    @Transactional(readOnly = true)
    public List<RoleWithUsersDTO> getAllRolesWithUsers() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream().map(role -> {
            List<UserDTO> usersInRole = userService.getUsersByRole(role.getName());

            Set<String> permissionNames = role.getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());

            return new RoleWithUsersDTO(
                    role.getId(),
                    role.getName(),
                    permissionNames,
                    usersInRole
            );
        }).collect(Collectors.toList());
    }

    /**
     * Creates a new role with the specified permissions.
     * <p>
     * Throws {@link DuplicateResourceException} if a role with the same name already exists.
     * Throws {@link ResourceNotFoundException} if any specified permission does not exist.
     * </p>
     *
     * @param createRoleDTO the DTO containing role name and permissions
     * @return the created {@link RoleDTO}
     */
    @Transactional
    public RoleDTO createRole(CreateRoleDTO createRoleDTO) {

        String trimmedName = createRoleDTO.name().trim();

        if (roleRepository.findByName(trimmedName).isPresent()) {
            throw new DuplicateResourceException("Role already exists: " + trimmedName);
        }

        Role role = new Role();
        role.setName(trimmedName);
        Set<Permission> permissions = createRoleDTO.permissions().stream()
                .map(name -> permissionRepository.findById(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + name)))
                .collect(Collectors.toSet());
        role.setPermissions(permissions);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toRoleDTO(savedRole);
    }

    /**
     * Updates the permissions assigned to an existing role.
     * <p>
     * Throws {@link ResourceNotFoundException} if the role or any specified permission does not exist.
     * </p>
     *
     * @param roleId          the ID of the role to update
     * @param permissionNames the set of permission names to assign to the role
     */
    @Transactional
    public void updateRolePermissions(Long roleId, Set<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        Set<Permission> permissions = permissionNames.stream()
                .map(name -> permissionRepository.findById(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + name)))
                .collect(Collectors.toSet());
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    /**
     * Deletes a role by its ID, only if it is not currently in use and not a core system role.
     * <p>
     * Throws {@link ResourceNotFoundException} if the role does not exist.
     * Throws {@link RoleInUseException} if the role is assigned to any users.
     * Throws {@link SelfActionException} if an attempt is made to delete core roles like ROLE_ADMIN or ROLE_USER.
     * </p>
     *
     * @param roleId the ID of the role to delete
     */
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (role.getName().equals(Role.ADMIN) || role.getName().equals(Role.USER)) {
            throw new SelfActionException("Cannot delete core system role: " + role.getName());
        }

        long userCount = userRepository.countByRoles_Id(roleId);
        if (userCount > 0) {
            throw new RoleInUseException(
                    "Cannot delete role. It is still assigned to " + userCount + " user(s)."
            );
        }

        roleRepository.delete(role);
    }

}
