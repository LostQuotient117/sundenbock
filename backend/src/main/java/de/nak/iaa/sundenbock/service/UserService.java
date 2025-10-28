package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.RoleDTO;
import de.nak.iaa.sundenbock.dto.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.mapper.UserMapper;
import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.model.user.Permission;
import de.nak.iaa.sundenbock.model.user.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userMapper = userMapper;
    }

    /**
     * Retrieves all users and converts them to safe DTOs.
     * @return A list of all users.
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toUserDTO)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Transactional
    public void assignPermissionToUser(Long userId, String permissionName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Permission permission = permissionRepository.findById(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        user.getPermissions().add(permission);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDetailDTO getDetailedUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toUserDetailDTO)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

}
