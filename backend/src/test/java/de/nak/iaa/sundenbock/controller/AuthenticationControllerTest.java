package de.nak.iaa.sundenbock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nak.iaa.sundenbock.config.JwtAuthFilter;
import de.nak.iaa.sundenbock.config.SecurityConfig;
import de.nak.iaa.sundenbock.dto.auth.AuthenticationRequest;
import de.nak.iaa.sundenbock.dto.auth.AuthenticationResponse;
import de.nak.iaa.sundenbock.dto.auth.ChangePasswordRequest;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDetailDTO;
import de.nak.iaa.sundenbock.exception.CustomAccessDeniedHandler;
import de.nak.iaa.sundenbock.exception.CustomAuthenticationEntryPoint;
import de.nak.iaa.sundenbock.exception.UserDisabledException;
import de.nak.iaa.sundenbock.service.security.AuthenticationService;
import de.nak.iaa.sundenbock.service.security.CustomSecurityService;
import de.nak.iaa.sundenbock.service.security.JwtService;
import de.nak.iaa.sundenbock.service.user.UserDetailsServiceImpl;
import de.nak.iaa.sundenbock.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        CustomSecurityService.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        UserDetailsServiceImpl.class
})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CustomSecurityService customSecurityService;

    // --- POST /register ---

    @Test
    @DisplayName("POST /api/v1/auth/register should create user and return 200")
    @WithAnonymousUser
    void register_shouldCreateUser_andReturn200() throws Exception {
        CreateUserDTO createRequest = new CreateUserDTO(
                "newuser", "New", "User", "new@example.com", "password123", null
        );
        AuthenticationResponse authResponse = new AuthenticationResponse("mock.token.jwt");

        when(authenticationService.register(any(CreateUserDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.token.jwt"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 for invalid data")
    @WithAnonymousUser
    void register_shouldReturn400_forInvalidData() throws Exception {
        CreateUserDTO badRequest = new CreateUserDTO(
                "a", "", "", "not-an-email", "123", null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // --- POST /authenticate ---

    @Test
    @DisplayName("POST /api/v1/auth/authenticate should return token for valid credentials")
    @WithAnonymousUser
    void authenticate_shouldReturnToken_forValidCredentials() throws Exception {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "password123");
        AuthenticationResponse authResponse = new AuthenticationResponse("mock.token.jwt");

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.token.jwt"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/authenticate should return 401 for bad credentials")
    @WithAnonymousUser
    void authenticate_shouldReturn401_forBadCredentials() throws Exception {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "wrongpassword");

        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/authenticate should return 403 for disabled user")
    @WithAnonymousUser
    void authenticate_shouldReturn403_forDisabledUser() throws Exception {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "password123");

        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new UserDisabledException("User is disabled"));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // --- GET /me ---

    @Test
    @DisplayName("GET /api/v1/auth/me should return 401 for anonymous user")
    @WithAnonymousUser
    void getMe_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return user details for authenticated user")
    @WithMockUser(username = "testuser")
    void getMe_shouldReturnUserDetails_forAuthenticatedUser() throws Exception {
        UserDetailDTO userDetailDTO = new UserDetailDTO(
                1L, "testuser", "Test", "User", "test@example.com",
                null, null, true, Set.of("ROLE_USER"), Set.of()
        );

        when(userService.getDetailedUserByUsername("testuser")).thenReturn(userDetailDTO);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    // --- PUT /change-password ---

    @Test
    @DisplayName("PUT /api/v1/auth/change-password should return 401 for anonymous user")
    @WithAnonymousUser
    void changePassword_shouldReturn401_forAnonymous() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/auth/change-password should return 200 for authenticated user")
    @WithMockUser(username = "testuser")
    void changePassword_shouldReturn200_forAuthenticatedUser() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass123");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/auth/change-password should return 400 for invalid data")
    @WithMockUser(username = "testuser")
    void changePassword_shouldReturn400_forInvalidData() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "123");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}