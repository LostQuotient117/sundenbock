package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.roleDTO.CreateRoleDTO;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO;
import de.nak.iaa.sundenbock.dto.mapper.RoleMapper;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
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

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleDTO createRole(CreateRoleDTO createRoleDTO) {
        Role role = new Role();
        role.setName(createRoleDTO.name());
        Set<Permission> permissions = createRoleDTO.permissions().stream()
                .map(name -> permissionRepository.findById(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + name)))
                .collect(Collectors.toSet());
        role.setPermissions(permissions);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toRoleDTO(savedRole);
    }

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

}
