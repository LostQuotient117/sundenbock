package de.nak.iaa.sundenbock.service.user;

import de.nak.iaa.sundenbock.dto.auth.AdminResetPasswordDTO;
import de.nak.iaa.sundenbock.dto.mapper.UserMapper;
import de.nak.iaa.sundenbock.dto.userDTO.*;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.SelfActionException;
import de.nak.iaa.sundenbock.exception.UserInUseException;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link UserService}.
 * We mock all dependencies (repositories, mappers, passwordEncoder) to test
 * the service's business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role adminRole;
    private Role userRole;
    private Permission testPermission;
    private UserDTO testUserDTO;
    private UserDetailDTO testUserDetailDTO;

    /**
     * Set up common test data and mock behavior before each test.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");

        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        userRole = new Role();
        userRole.setName("ROLE_USER");

        testUser.getRoles().add(userRole);

        testPermission = new Permission();
        testPermission.setName("USER_MANAGE");

        testUserDTO = new UserDTO(1L, "testuser", "Test", "User");
        testUserDetailDTO = new UserDetailDTO(
                1L, "testuser", "Test", "User", "test@example.com",
                LocalDateTime.now(), LocalDateTime.now(), true,
                Set.of("ROLE_USER"), Set.of()
        );
    }

    /**
     * Clean up the SecurityContext after each test.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Helper method to mock the currently authenticated user.
     * This is necessary for testing SelfActionExceptions.
     */
    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    // --- Get Methods ---

    @Test
    @DisplayName("getAllUsers should return list of UserDTOs")
    void getAllUsers_shouldReturnUserDTOList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(userMapper.toUserDTO(testUser)).thenReturn(testUserDTO);

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().username()).isEqualTo("testuser");
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toUserDTO(testUser);
    }

    @Test
    @DisplayName("getUserByUsername should return UserDTO when user exists")
    void getUserByUsername_shouldReturnUserDTO_whenUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDTO(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.getUserByUsername("testuser");

        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userMapper, times(1)).toUserDTO(testUser);
    }

    @Test
    @DisplayName("getUserByUsername should throw RNF when user not found")
    void getUserByUsername_shouldThrowResourceNotFoundException_whenUserNotExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username: unknown");
    }

    @Test
    @DisplayName("getDetailedUserByUsername should return UserDetailDTO when user exists")
    void getDetailedUserByUsername_shouldReturnUserDetailDTO_whenUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDetailDTO(testUser)).thenReturn(testUserDetailDTO);

        UserDetailDTO result = userService.getDetailedUserByUsername("testuser");

        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userMapper, times(1)).toUserDetailDTO(testUser);
    }

    @Test
    @DisplayName("getDetailedUserByUsername should throw RNF when user not found")
    void getDetailedUserByUsername_shouldThrowResourceNotFoundException_whenUserNotExists() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getDetailedUserByUsername("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Create / Update Methods ---

    @Test
    @DisplayName("createUser should succeed with default role when roles are empty")
    void createUser_shouldCreateUserWithDefaultRole_whenRolesAreEmpty() {
        CreateUserDTO request = new CreateUserDTO("newuser", "New", "User", "new@example.com", "password123", Set.of());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRoles().contains(userRole)
        ));
    }

    @Test
    @DisplayName("createUser should succeed with specified roles")
    void createUser_shouldCreateUserWithSpecifiedRoles() {
        CreateUserDTO request = new CreateUserDTO("newadmin", "New", "Admin", "admin@example.com", "password123", Set.of("ROLE_ADMIN"));
        when(userRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newadmin") &&
                        user.getRoles().contains(adminRole) &&
                        !user.getRoles().contains(userRole)
        ));
        verify(roleRepository, never()).findByName("ROLE_USER");
    }

    @Test
    @DisplayName("createUser should throw DuplicateResourceException when username exists")
    void createUser_shouldThrowDuplicateResourceException_whenUsernameExists() {
        CreateUserDTO request = new CreateUserDTO("testuser", "New", "User", "new@example.com", "password123", Set.of());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists: testuser");
    }

    @Test
    @DisplayName("createUser should throw DuplicateResourceException when email exists")
    void createUser_shouldThrowDuplicateResourceException_whenEmailExists() {
        CreateUserDTO request = new CreateUserDTO("newuser", "New", "User", "new@example.com", "password123", Set.of());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already in use: new@example.com");
    }

    @Test
    @DisplayName("updateUser should update fields correctly")
    void updateUser_shouldUpdateFields() {
        UpdateUserDTO updateDto = new UpdateUserDTO("UpdatedFirst", "UpdatedLast", "updated@example.com", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser("testuser", updateDto);

        verify(userRepository).save(argThat(user ->
                user.getFirstName().equals("UpdatedFirst") &&
                        user.getLastName().equals("UpdatedLast") &&
                        user.getEmail().equals("updated@example.com") &&
                        !user.isEnabled()
        ));
    }

    @Test
    @DisplayName("updateUser should throw DuplicateResourceException when email is in use")
    void updateUser_shouldThrowDuplicateResourceException_whenEmailIsInUse() {
        UpdateUserDTO updateDto = new UpdateUserDTO(null, null, "taken@example.com", null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser("testuser", updateDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email taken@example.com is already in use.");
    }

    @Test
    @DisplayName("adminResetPassword should encode and save new password")
    void adminResetPassword_shouldEncodeAndSaveNewPassword() {
        AdminResetPasswordDTO request = new AdminResetPasswordDTO("newStrongPassword123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newStrongPassword123")).thenReturn("newEncodedPassword");

        userService.adminResetPassword("testuser", request);

        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("newEncodedPassword")
        ));
    }

    // --- Delete Methods ---

    @Test
    @DisplayName("deleteUserByUsername should throw SelfActionException with hint to deactivate")
    void deleteUserByUsername_shouldThrowSelfActionException_whenUserDeletesSelf() {
        mockSecurityContext("testuser");

        assertThatThrownBy(() -> userService.deleteUserByUsername("testuser"))
                .isInstanceOf(SelfActionException.class)
                .hasMessageContaining("You cannot delete your own account. You can deactivate it instead.");

        verify(userRepository, never()).deleteByUsername(anyString());
    }

    @Test
    @DisplayName("deleteUserByUsername should throw UserInUseException when user is still referenced")
    void deleteUserByUsername_shouldThrowUserInUseException_whenUserIsInUse() {
        mockSecurityContext("admin");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(projectRepository.countByCreatedBy(testUser)).thenReturn(1L);
        when(projectRepository.countByLastModifiedBy(testUser)).thenReturn(0L);
        when(ticketRepository.countByResponsiblePerson(testUser)).thenReturn(0L);
        when(ticketRepository.countByCreatedBy(testUser)).thenReturn(0L);
        when(commentRepository.countByCreatedBy(testUser)).thenReturn(0L);

        assertThatThrownBy(() -> userService.deleteUserByUsername("testuser"))
                .isInstanceOf(UserInUseException.class)
                .hasMessageContaining("Cannot delete user 'testuser'. It is still in use:");

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUserByUsername should delete user when user is not in use")
    void deleteUserByUsername_shouldDeleteUser_whenUserIsNotInUse() {
        mockSecurityContext("admin");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(projectRepository.countByCreatedBy(testUser)).thenReturn(0L);
        when(projectRepository.countByLastModifiedBy(testUser)).thenReturn(0L);
        when(ticketRepository.countByResponsiblePerson(testUser)).thenReturn(0L);
        when(ticketRepository.countByCreatedBy(testUser)).thenReturn(0L);
        when(commentRepository.countByCreatedBy(testUser)).thenReturn(0L);

        userService.deleteUserByUsername("testuser");

        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("deleteUserByUsername should throw RNF when user not exists")
    void deleteUserByUsername_shouldThrowResourceNotFoundException_whenUserNotExists() {
        mockSecurityContext("admin");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserByUsername("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username: unknown");
    }

    // --- Deactivate Self ---

    @Test
    @DisplayName("deactivateSelf should set user to disabled")
    void deactivateSelf_shouldSetEnabledFalse() {
        mockSecurityContext("testuser");
        testUser.setEnabled(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        userService.deactivateSelf();

        verify(userRepository).save(argThat(user -> !user.isEnabled()));
    }

    @Test
    @DisplayName("deactivateSelf should throw SelfActionException for 'system' user")
    void deactivateSelf_shouldThrowException_forSystemUser() {
        mockSecurityContext("system");

        assertThatThrownBy(() -> userService.deactivateSelf())
                .isInstanceOf(SelfActionException.class)
                .hasMessageContaining("system' user cannot be deactivated");

        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Activate User (Admin) ---

    @Test
    @DisplayName("activateUser should set user to enabled")
    void activateUser_shouldSetEnabledTrue() {
        testUser.setEnabled(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        userService.activateUser("testuser");

        verify(userRepository).save(argThat(User::isEnabled));
    }

    @Test
    @DisplayName("activateUser should throw SelfActionException for 'system' user")
    void activateUser_shouldThrowSelfActionException_forSystemUser() {
        assertThatThrownBy(() -> userService.activateUser("system"))
                .isInstanceOf(SelfActionException.class)
                .hasMessageContaining("system' user account cannot be re-activated");

        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("activateUser should throw RNF when user not found")
    void activateUser_shouldThrowRNF_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.activateUser("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Role Assignment ---

    @Test
    @DisplayName("assignRoleToUser should add role to user")
    void assignRoleToUser_shouldAddRoleToUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        assertThat(testUser.getRoles()).doesNotContain(adminRole);

        userService.assignRoleToUser("testuser", "ROLE_ADMIN");

        verify(userRepository).save(argThat(user ->
                user.getRoles().contains(adminRole)
        ));
    }

    @Test
    @DisplayName("removeRoleFromUser should throw SelfActionException when removing own ADMIN role")
    void removeRoleFromUser_shouldThrowSelfActionException_whenRemovingOwnAdminRole() {
        mockSecurityContext("testuser");
        testUser.getRoles().add(adminRole);

        assertThatThrownBy(() -> userService.removeRoleFromUser("testuser", "ROLE_ADMIN"))
                .isInstanceOf(SelfActionException.class)
                .hasMessageContaining("You cannot remove the ADMIN role from your own account.");
    }

    @Test
    @DisplayName("removeRoleFromUser should throw SelfActionException when removing last role")
    void removeRoleFromUser_shouldThrowSelfActionException_whenRemovingLastRole() {
        mockSecurityContext("testuser");

        assertThat(testUser.getRoles()).hasSize(1);
        assertThat(testUser.getRoles()).contains(userRole);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));

        assertThatThrownBy(() -> userService.removeRoleFromUser("testuser", "ROLE_USER"))
                .isInstanceOf(SelfActionException.class)
                .hasMessageContaining("You cannot remove the only role from your own account.");
    }

    // --- Permission Assignment ---

    @Test
    @DisplayName("assignPermissionToUser should add permission")
    void assignPermissionToUser_shouldAddPermission() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(permissionRepository.findById("USER_MANAGE")).thenReturn(Optional.of(testPermission));

        userService.assignPermissionToUser("testuser", "USER_MANAGE");

        verify(userRepository).save(argThat(user ->
                user.getPermissions().contains(testPermission)
        ));
    }

    @Test
    @DisplayName("assignPermissionToUser should throw RNF when permission not found")
    void assignPermissionToUser_shouldThrowResourceNotFoundException_whenPermissionNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(permissionRepository.findById("FAKE_PERM")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignPermissionToUser("testuser", "FAKE_PERM"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Permission not found: FAKE_PERM");
    }

    @Test
    @DisplayName("removePermissionFromUser should throw SelfActionException when removing own core permission")
    void removePermissionFromUser_shouldThrowSelfActionException_whenRemovingOwnUserManagePermission() {
        mockSecurityContext("testuser");
        testUser.getPermissions().add(testPermission);

        assertThatThrownBy(() -> userService.removePermissionFromUser("testuser", "USER_MANAGE"))
                .isInstanceOf(SelfActionException.class)
                .hasMessageContaining("You cannot remove core administrative permissions");
    }

    @Test
    @DisplayName("removePermissionFromUser should remove permission")
    void removePermissionFromUser_shouldRemovePermission() {
        mockSecurityContext("admin");
        testUser.getPermissions().add(testPermission);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(permissionRepository.findById("USER_MANAGE")).thenReturn(Optional.of(testPermission));

        userService.removePermissionFromUser("testuser", "USER_MANAGE");

        verify(userRepository).save(argThat(user ->
                user.getPermissions().isEmpty()
        ));
    }

    // --- Role Assignment (Unhappy Paths) ---

    @Test
    @DisplayName("assignRoleToUser should throw RNF when user not found")
    void assignRoleToUser_shouldThrowRNF_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRoleToUser("unknown", "ROLE_ADMIN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("assignRoleToUser should throw RNF when role not found")
    void assignRoleToUser_shouldThrowRNF_whenRoleNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("FAKE_ROLE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRoleToUser("testuser", "FAKE_ROLE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");
    }

    // --- Role Removal (Happy Path & Edge Case) ---

    @Test
    @DisplayName("removeRoleFromUser should successfully remove role")
    void removeRoleFromUser_shouldRemoveRole() {
        mockSecurityContext("admin");
        testUser.getRoles().add(adminRole);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        userService.removeRoleFromUser("testuser", "ROLE_ADMIN");

        verify(userRepository).save(argThat(user ->
                !user.getRoles().contains(adminRole) &&
                        user.getRoles().contains(userRole)
        ));
    }

    @Test
    @DisplayName("removeRoleFromUser should re-add default role if last role is removed")
    void removeRoleFromUser_shouldReAddDefaultRole_whenLastRoleIsRemoved() {
        mockSecurityContext("admin");
        testUser.setRoles(new java.util.HashSet<>(Set.of(adminRole)));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));

        userService.removeRoleFromUser("testuser", "ROLE_ADMIN");

        verify(userRepository).save(argThat(user ->
                !user.getRoles().contains(adminRole) &&
                        user.getRoles().contains(userRole)
        ));
    }

    // --- Permission Assignment (Unhappy Path) ---

    @Test
    @DisplayName("assignPermissionToUser should throw RNF when user not found")
    void assignPermissionToUser_shouldThrowRNF_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignPermissionToUser("unknown", "USER_MANAGE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // --- Permission Removal (Unhappy Paths) ---

    @Test
    @DisplayName("removePermissionFromUser should throw RNF when user not found")
    void removePermissionFromUser_shouldThrowRNF_whenUserNotFound() {
        mockSecurityContext("admin");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removePermissionFromUser("unknown", "USER_MANAGE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("removePermissionFromUser should throw RNF when permission not found")
    void removePermissionFromUser_shouldThrowRNF_whenPermissionNotFound() {
        mockSecurityContext("admin");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(permissionRepository.findById("FAKE_PERM")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removePermissionFromUser("testuser", "FAKE_PERM"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Permission not found");
    }
}