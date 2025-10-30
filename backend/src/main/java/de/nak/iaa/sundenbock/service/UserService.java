package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.auth.RegistrationRequest;
import de.nak.iaa.sundenbock.dto.mapper.UserMapper;
import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.model.user.Permission;
import de.nak.iaa.sundenbock.model.user.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserDTO)
                .orElseThrow(() -> new RuntimeException("User not found with Username: " + username));
    }

    @Transactional(readOnly = true)
    public UserDetailDTO getDetailedUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserDetailDTO)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Transactional
    public UserDetailDTO updateUser(String username, UserDetailDTO userDetailDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(userDetailDTO.email());
        user.setEnabled(userDetailDTO.enabled());
        // hier maybe noch role / permission updates

        User updatedUser = userRepository.save(user);
        return userMapper.toUserDetailDTO(updatedUser);
    }

    @Transactional
    public UserDetailDTO createUser(RegistrationRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists: " + request.username());
        }

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(defaultRole));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return userMapper.toUserDetailDTO(savedUser);
    }

    @Transactional
    public void deleteUserByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new RuntimeException("User not found with username: " + username);
        }
        userRepository.deleteByUsername(username);
    }

    // maybe not needed
    @Transactional
    public void assignRoleToUser(String username, Long roleId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Transactional
    public void assignPermissionToUser(String username, String permissionName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Permission permission = permissionRepository.findById(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        user.getPermissions().add(permission);
        userRepository.save(user);
    }


}
