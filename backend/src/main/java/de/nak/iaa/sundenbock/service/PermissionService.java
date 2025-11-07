package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.dto.mapper.PermissionMapper;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.PermissionInUseException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for permission management.
 * <p>
 * Provides methods to list all permissions and to create new permissions.
 */
@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public PermissionService(PermissionRepository permissionRepository, PermissionMapper permissionMapper, RoleRepository roleRepository, UserRepository userRepository) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all existing permissions as a list of {@link PermissionDTO}.
     *
     * @return a list of all permissions
     */
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toPermissionDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new permission based on the provided {@link PermissionDTO}.
     * <p>
     * Throws {@link DuplicateResourceException} if a permission with the same name already exists.
     * </p>
     *
     * @param permissionDTO the DTO containing the permission data
     * @return the created {@link PermissionDTO}
     */
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {

        String trimmedName = permissionDTO.name().trim();

        if (permissionRepository.existsById(trimmedName)) {
            throw new DuplicateResourceException("Permission already exists: " + trimmedName);
        }

        Permission permission = new Permission();
        permission.setName(trimmedName);
        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toPermissionDTO(savedPermission);
    }

    /**
     * Deletes a permission by its name, only if it is not currently in use.
     * <p>
     * Throws {@link ResourceNotFoundException} if the permission does not exist.
     * Throws {@link PermissionInUseException} if the permission is assigned to any roles or users.
     * </p>
     *
     * @param permissionName the name of the permission to delete
     */
    @Transactional
    public void deletePermission(String permissionName) {
        if (!permissionRepository.existsById(permissionName.trim())) {
            throw new ResourceNotFoundException("Permission not found: " + permissionName.trim());
        }

        long rolesCount = roleRepository.countByPermissions_Name(permissionName);
        if (rolesCount > 0) {
            throw new PermissionInUseException(
                    "Cannot delete permission. It is still used by " + rolesCount + " role(s)."
            );
        }

        long usersCount = userRepository.countByPermissions_Name(permissionName);
        if (usersCount > 0) {
            throw new PermissionInUseException(
                    "Cannot delete permission. It is still directly assigned to " + usersCount + " user(s)."
            );
        }

        permissionRepository.deleteById(permissionName);
    }

}
