package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.userDTO.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.dto.mapper.UserMapper;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.SelfActionException;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
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
     *
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Username: " + username));
    }

    @Transactional(readOnly = true)
    public UserDetailDTO getDetailedUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserDetailDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Updates an existing user's details.
     * This method resolves the sets of role and permission *names* (Strings) from the DTO
     * into managed JPA entities and overwrites the user's associations.
     *
     * @param username      The username of the user to update.
     * @param userDetailDTO The DTO containing the new data (email, enabled, roles, permissions).
     * @return The updated UserDetailDTO.
     * @throws ResourceNotFoundException if the user, a role, or a permission is not found.
     */
    @Transactional
    public UserDetailDTO updateUser(String username, UserDetailDTO userDetailDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmail(userDetailDTO.email());
        user.setEnabled(userDetailDTO.enabled());

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUsername.equals(username) && userDetailDTO.roles().isEmpty()) {
            throw new SelfActionException("You cannot remove all roles from your own account.");
        }

        Set<Role> roles = userDetailDTO.roles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        Set<Permission> permissions = userDetailDTO.permissions().stream()
                .map(permissionName -> permissionRepository.findById(permissionName)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName)))
                .collect(Collectors.toSet());
        user.setPermissions(permissions);

        if (user.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not found"));
            user.getRoles().add(userRole);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserDetailDTO(updatedUser);
    }

    /**
     * Creates a new user (administrative creation).
     * <p>
     * If roles are provided in the request, they are assigned.
     * If no roles are provided, the user defaults to "ROLE_USER".
     *
     * @param request The DTO with registration details.
     * @return The DTO of the created user.
     * @throws DuplicateResourceException if the username already exists.
     * @throws ResourceNotFoundException if a specified role does not exist.
     */
    @Transactional
    public UserDetailDTO createUser(CreateUserDTO request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);

        Set<Role> rolesToAssign;
        if (request.roles() == null || request.roles().isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not found"));
            rolesToAssign = Set.of(defaultRole);
        } else {
            rolesToAssign = request.roles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
        }
        user.setRoles(rolesToAssign);

        User savedUser = userRepository.save(user);

        return userMapper.toUserDetailDTO(savedUser);
    }

    /**
     * Deletes a user by their username.
     * <p>
     * Prevents the currently authenticated user from deleting their own account.
     * Throws an exception if the user does not exist.
     *
     * @param username the username of the user to delete
     * @throws SelfActionException if the user attempts to delete their own account
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional
    public void deleteUserByUsername(String username) {

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUsername.equals(username)) {
            throw new SelfActionException("You cannot delete your own account.");
        }

        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        userRepository.deleteByUsername(username);
    }

    /**
     * Assigns a role to a user.
     * <p>
     * Resolves the user and role by their respective identifiers and adds the role to the user's roles.
     *
     * @param username the username of the user
     * @param roleName the name of the role to assign
     * @throws ResourceNotFoundException if the user or role does not exist
     */
    @Transactional
    public void assignRoleToUser(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    /**
     * Assigns a permission to a user.
     * <p>
     * Resolves the user and permission by their respective identifiers and adds the permission to the user's permissions.
     *
     * @param username the username of the user
     * @param permissionName the name of the permission to assign
     * @throws ResourceNotFoundException if the user or permission does not exist
     */
    @Transactional
    public void assignPermissionToUser(String username, String permissionName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Permission permission = permissionRepository.findById(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        user.getPermissions().add(permission);
        userRepository.save(user);
    }

    /**
     * Removes a role from a user.
     * <p>
     * Prevents the currently authenticated user from removing the "ROLE_ADMIN" role from their own account.
     * Resolves the user and role by their respective identifiers and removes the role from the user's roles.
     *
     * @param username the username of the user
     * @param roleName the name of the role to remove
     * @throws SelfActionException if the user attempts to remove the "ROLE_ADMIN" role from their own account
     * @throws ResourceNotFoundException if the user or role does not exist
     */
    @Transactional
    public void removeRoleFromUser(String username, String roleName) {

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUsername.equals(username) && roleName.equals("ROLE_ADMIN")) {
            throw new SelfActionException("You cannot remove the ADMIN role from your own account.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (currentUsername.equals(username) && user.getRoles().size() == 1) {
            throw new SelfActionException("You cannot remove the only role from your own account.");
        }

        user.getRoles().remove(role);

        if (user.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not found"));
            user.getRoles().add(userRole);
        }

        userRepository.save(user);
    }

    /**
     * Removes a permission from a user.
     * <p>
     * Prevents the currently authenticated user from removing core administrative permissions
     * ("USER_MANAGE" or "ROLE_MANAGE") from their own account.
     * Resolves the user and permission by their respective identifiers and removes the permission from the user's permissions.
     *
     * @param username the username of the user
     * @param permissionName the name of the permission to remove
     * @throws SelfActionException if the user attempts to remove core administrative permissions from their own account
     * @throws ResourceNotFoundException if the user or permission does not exist
     */
    @Transactional
    public void removePermissionFromUser(String username, String permissionName) {

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUsername.equals(username) &&
                (permissionName.equals("USER_MANAGE") || permissionName.equals("ROLE_MANAGE"))) {
            throw new SelfActionException("You cannot remove core administrative permissions from your own account.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Permission permission = permissionRepository.findById(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        user.getPermissions().remove(permission);
        userRepository.save(user);
    }

}
