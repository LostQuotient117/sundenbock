package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.auth.AdminResetPasswordDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UpdateUserDTO;
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
        User user = findUserByUsername(username);
        return userMapper.toUserDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDetailDTO getDetailedUserByUsername(String username) {
        User user = findUserByUsername(username);
        return userMapper.toUserDetailDTO(user);
    }

    /**
     * Updates an existing user specified by username.
     * This method performs a partial update (a "merge") of mutable fields.
     * Only non-null fields (email, enabled) from the updateDto will be applied.
     * The 'username' is immutable and cannot be changed.
     *
     * @param username  The username of the user to update.
     * @param updateDto The DTO containing the fields to update.
     * @return The updated, saved User as a UserDetailDTO.
     */
    @Transactional
    public UserDetailDTO updateUser(String username, UpdateUserDTO updateDto) {

        User user = findUserByUsername(username);

        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new DuplicateResourceException("Email " + updateDto.getEmail() + " is already in use.");
            }
            user.setEmail(updateDto.getEmail());
        }

        if (updateDto.getEnabled() != null) {
            user.setEnabled(updateDto.getEnabled());
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserDetailDTO(savedUser);
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

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);

        Set<Role> rolesToAssign;
        if (request.roles() == null || request.roles().isEmpty()) {
            rolesToAssign = Set.of(getDefaultUserRole());
        } else {
            rolesToAssign = request.roles().stream()
                    .map(this::findRoleByName)
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
        if (isCurrentUser(username)) {
            throw new SelfActionException("You cannot delete your own account.");
        }

        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        userRepository.deleteByUsername(username);
    }

    /**
     * Resets a user's password (administrative action).
     * This method bypasses the "old password" check and allows an admin
     * to set a new password for any user.
     *
     * @param username the user whose password will be reset
     * @param request  the DTO containing the new password
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public void adminResetPassword(String username, AdminResetPasswordDTO request) {
        User user = findUserByUsername(username);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
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
        User user = findUserByUsername(username);
        Role role = findRoleByName(roleName);
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
        User user = findUserByUsername(username);
        Permission permission = findPermissionByName(permissionName);
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

        if (isCurrentUser(username) && roleName.equals("ROLE_ADMIN")) {
            throw new SelfActionException("You cannot remove the ADMIN role from your own account.");
        }

        User user = findUserByUsername(username);
        Role role = findRoleByName(roleName);

        if (isCurrentUser(username) && user.getRoles().size() == 1) {
            throw new SelfActionException("You cannot remove the only role from your own account.");
        }

        user.getRoles().remove(role);

        if (user.getRoles().isEmpty()) {
            user.getRoles().add(getDefaultUserRole());
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
        if (isCurrentUser(username) &&
                (permissionName.equals("USER_MANAGE") || permissionName.equals("ROLE_MANAGE"))) {
            throw new SelfActionException("You cannot remove core administrative permissions from their own account.");
        }

        User user = findUserByUsername(username);
        Permission permission = findPermissionByName(permissionName);
        user.getPermissions().remove(permission);
        userRepository.save(user);
    }

    // Helper methods

    /**
     * Holt einen User anhand des Usernamens oder wirft eine ResourceNotFoundException.
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Holt eine Rolle anhand des Namens oder wirft eine ResourceNotFoundException.
     */
    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }

    /**
     * Holt eine Permission anhand des Namens oder wirft eine ResourceNotFoundException.
     */
    private Permission findPermissionByName(String permissionName) {
        return permissionRepository.findById(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName));
    }

    /**
     * Holt die Standard-Benutzerrolle ("ROLE_USER").
     */
    private Role getDefaultUserRole() {
        return roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not found"));
    }

    /**
     * Holt den Benutzernamen des aktuell authentifizierten Benutzers.
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Prüft, ob der angegebene Username der aktuell eingeloggte Benutzer ist.
     */
    private boolean isCurrentUser(String username) {
        return getCurrentUsername().equals(username);
    }
}
