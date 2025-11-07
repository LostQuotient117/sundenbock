package de.nak.iaa.sundenbock.service.security;

import de.nak.iaa.sundenbock.dto.auth.AuthenticationRequest;
import de.nak.iaa.sundenbock.dto.auth.AuthenticationResponse;
import de.nak.iaa.sundenbock.dto.auth.ChangePasswordRequest;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.UserDisabledException;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private Role devRole;
    private CreateUserDTO registerRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        devRole = new Role();
        devRole.setName("ROLE_DEVELOPER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedOldPassword");
        testUser.setRoles(Set.of(devRole));

        registerRequest = new CreateUserDTO(
                "testuser", "Test", "User", "test@example.com", "password123", null
        );

        userDetails = mock(UserDetails.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    // --- Register ---

    @Test
    @DisplayName("register should create user and return token")
    void register_shouldCreateUserAndReturnToken() {
        when(roleRepository.findByName("ROLE_DEVELOPER")).thenReturn(Optional.of(devRole));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked.jwt.token");

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertThat(response.token()).isEqualTo("mocked.jwt.token");
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("testuser") &&
                        user.getRoles().contains(devRole)
        ));
    }

    @Test
    @DisplayName("register should throw RNF when default role not found")
    void register_shouldThrowResourceNotFoundException_whenRoleNotFound() {
        when(roleRepository.findByName("ROLE_DEVELOPER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Default role not found");
    }

    @Test
    @DisplayName("register should throw DuplicateResourceException when username exists")
    void register_shouldThrowDuplicateResourceException_whenUsernameExists() {
        when(roleRepository.findByName("ROLE_DEVELOPER")).thenReturn(Optional.of(devRole));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists: testuser");
    }

    @Test
    @DisplayName("register should throw DuplicateResourceException when email exists")
    void register_shouldThrowDuplicateResourceException_whenEmailExists() {
        when(roleRepository.findByName("ROLE_DEVELOPER")).thenReturn(Optional.of(devRole));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already in use: test@example.com");
    }

    // --- Authenticate ---

    @Test
    @DisplayName("authenticate should return token on success")
    void authenticate_shouldReturnTokenOnSuccess() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "password123");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked.jwt.token");

        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        assertThat(response.token()).isEqualTo("mocked.jwt.token");
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "password123")
        );
    }

    @Test
    @DisplayName("authenticate should throw UserDisabledException")
    void authenticate_shouldThrowUserDisabledException_whenAccountDisabled() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "password123");
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("User is disabled"));

        assertThatThrownBy(() -> authenticationService.authenticate(authRequest))
                .isInstanceOf(UserDisabledException.class)
                .hasMessageContaining("Your account is disabled");
    }

    @Test
    @DisplayName("authenticate should re-throw BadCredentialsException")
    void authenticate_shouldThrowBadCredentialsException_whenCredentialsInvalid() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "wrongpassword");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authenticationService.authenticate(authRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // --- Change Password ---

    @Test
    @DisplayName("changePassword should update password when old password matches")
    void changePassword_shouldUpdatePassword_whenOldPasswordMatches() {
        mockSecurityContext("testuser");
        ChangePasswordRequest changeRequest = new ChangePasswordRequest("oldPass", "newPass");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPassword");

        authenticationService.changePassword(changeRequest);

        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedNewPassword")
        ));
    }

    @Test
    @DisplayName("changePassword should throw BadCredentialsException when old password mismatch")
    void changePassword_shouldThrowBadCredentialsException_whenOldPasswordMismatch() {
        mockSecurityContext("testuser");
        ChangePasswordRequest changeRequest = new ChangePasswordRequest("wrongOldPass", "newPass");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.changePassword(changeRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid old password");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword should throw RNF when user not found")
    void changePassword_shouldThrowResourceNotFoundException_whenUserNotFound() {
        mockSecurityContext("testuser");
        ChangePasswordRequest changeRequest = new ChangePasswordRequest("oldPass", "newPass");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.changePassword(changeRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Current user not found in database");
    }
}