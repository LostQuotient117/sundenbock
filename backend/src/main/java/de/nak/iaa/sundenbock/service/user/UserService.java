package de.nak.iaa.sundenbock.service.user;

import de.nak.iaa.sundenbock.dto.auth.AdminResetPasswordDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UpdateUserDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.dto.mapper.UserMapper;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.SelfActionException;
import de.nak.iaa.sundenbock.exception.UserInUseException;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides the core business logic for user management.
 * <p>
 * This service class acts as an intermediary between the controllers and the data layer
 * (repositories), encapsulating all business rules related to users, roles, and permissions.
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 * <li><b>User CRUD:</b> Handles the creation, retrieval, updating, and deletion of {@link User} entities.</li>
 * <li><b>Data Validation:</b> Ensures data integrity, such as checking for duplicate usernames or emails
 * (throwing {@link DuplicateResourceException}).</li>
 * <li><b>DTO Mapping:</b> Uses {@link UserMapper} to convert between persistence entities ({@link User})
 * and data transfer objects ({@link UserDTO}, {@link UserDetailDTO}, etc.).</li>
 * <li><b>Role & Permission Management:</b> Provides methods to assign and revoke roles and permissions
 * from users.</li>
 * <li><b>Security Operations:</b> Includes security-sensitive logic like password encoding
 * (via {@link PasswordEncoder}) and administrative password resets.</li>
 * <li><b>Safeguards:</b> Implements checks to prevent users from performing harmful operations on
 * their own accounts, such as self-deletion or removing their own admin rights
 * (throwing {@link SelfActionException}).</li>
 * </ul>
 * <p>
 * All data-modifying operations are transactional.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, ProjectRepository projectRepository, TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
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

        String trimmedEmail = (updateDto.email() != null) ? updateDto.email().trim() : null;
        String trimmedFirstName = (updateDto.firstName() != null) ? updateDto.firstName().trim() : null;
        String trimmedLastName = (updateDto.lastName() != null) ? updateDto.lastName().trim() : null;

        if (trimmedEmail != null && !trimmedEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(trimmedEmail)) {
                throw new DuplicateResourceException("Email " + trimmedEmail + " is already in use.");
            }
            user.setEmail(trimmedEmail);
        }

        if (trimmedFirstName != null) {
            user.setFirstName(trimmedFirstName);
        }

        if (trimmedLastName != null) {
            user.setLastName(trimmedLastName);
        }

        if (updateDto.enabled() != null) {
            user.setEnabled(updateDto.enabled());
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

        CreateUserDTO trimmedRequest = new CreateUserDTO(
                request.username().trim(),
                request.firstName().trim(),
                request.lastName().trim(),
                request.email().trim(),
                request.password(),
                (request.roles() != null) ? request.roles().stream().map(String::trim).collect(Collectors.toSet()) : Set.of());

        if (userRepository.findByUsername(trimmedRequest.username()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + trimmedRequest.username());
        }

        if (userRepository.existsByEmail(trimmedRequest.email())) {
            throw new DuplicateResourceException("Email already in use: " + trimmedRequest.email());
        }

        User user = new User();
        user.setUsername(trimmedRequest.username());
        user.setEmail(trimmedRequest.email());
        user.setPassword(passwordEncoder.encode(trimmedRequest.password()));
        user.setFirstName(trimmedRequest.firstName());
        user.setLastName(trimmedRequest.lastName());
        user.setEnabled(true);

        Set<Role> rolesToAssign;
        if (trimmedRequest.roles() == null || trimmedRequest.roles().isEmpty()) {
            rolesToAssign = Set.of(getDefaultUserRole());
        } else {
            rolesToAssign = trimmedRequest.roles().stream()
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
     * Throws {@link UserInUseException} if the user is still referenced by any projects,
     * tickets or comments.
     *
     * @param username the username of the user to delete
     * @throws SelfActionException if the user attempts to delete their own account
     * @throws ResourceNotFoundException if the user does not exist
     * @throws UserInUseException if the user is still in use
     */
    @Transactional
    public void deleteUserByUsername(String username) {
        if (isCurrentUser(username)) {
            throw new SelfActionException("You cannot delete your own account. You can deactivate it instead.");
        }

        User user = findUserByUsername(username);

        long projectsCreated = projectRepository.countByCreatedBy(user);
        long projectsModified = projectRepository.countByLastModifiedBy(user);
        long ticketsResponsible = ticketRepository.countByResponsiblePerson(user);
        long ticketsCreated = ticketRepository.countByCreatedBy(user);
        long commentsCreated = commentRepository.countByCreatedBy(user);

        long totalUsage = projectsCreated + projectsModified + ticketsResponsible + ticketsCreated + commentsCreated;

        if (totalUsage > 0) {
            StringBuilder message = new StringBuilder();
            message.append(String.format("Cannot delete user '%s'. It is still in use:", username));
            if (projectsCreated > 0) message.append(String.format(" %d projects (Creator),", projectsCreated));
            if (projectsModified > 0) message.append(String.format(" %d projects (Modifier),", projectsModified));
            if (ticketsResponsible > 0) message.append(String.format(" %d tickets (Responsible),", ticketsResponsible));
            if (ticketsCreated > 0) message.append(String.format(" %d tickets (Creator),", ticketsCreated));
            if (commentsCreated > 0) message.append(String.format(" %d comments (Creator),", commentsCreated));

            message.deleteCharAt(message.length() - 1);
            message.append(". You can deactivate the account instead.");

            throw new UserInUseException(message.toString());
        }

        userRepository.delete(user);
    }

    /**
     * Deactivates the currently authenticated user's account.
     *
     * This is the preferred way for a user to "delete" their own account,
     * as it preserves referential integrity for their created tickets/comments.
     * A deactivated user can no longer log in.
     *
     * @throws SelfActionException if the "system" user attempts to deactivate itself.
     * @throws ResourceNotFoundException if the authenticated user cannot be found.
     */
    @Transactional
    public void deactivateSelf() {
        String username = getCurrentUsername();
        if ("system".equals(username)) {
            throw new SelfActionException("The 'system' user cannot be deactivated.");
        }
        User user = findUserByUsername(username);
        user.setEnabled(false);
        userRepository.save(user);
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
     * Reactivates a user's account (administrative action).
     * This method sets the user's 'enabled' flag back to 'true'.
     *
     * @param username the user whose account will be reactivated
     * @throws SelfActionException if an attempt is made to activate the 'system' user
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public void activateUser(String username) {
        if ("system".equals(username)) {
            throw new SelfActionException("The 'system' user account cannot be re-activated.");
        }
        User user = findUserByUsername(username);
        user.setEnabled(true);
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

        if (isCurrentUser(username) && roleName.equals(Role.ADMIN)) {
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
                (permissionName.equals(Permission.USER_MANAGE) || permissionName.equals(Permission.ROLE_MANAGE))) {
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
        return roleRepository.findByName(Role.USER)
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
