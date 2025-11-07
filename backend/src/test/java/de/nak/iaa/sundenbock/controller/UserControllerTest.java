package de.nak.iaa.sundenbock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nak.iaa.sundenbock.config.JwtAuthFilter;
import de.nak.iaa.sundenbock.config.SecurityConfig;
import de.nak.iaa.sundenbock.dto.auth.AdminResetPasswordDTO;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UpdateUserDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDetailDTO;
import de.nak.iaa.sundenbock.exception.CustomAccessDeniedHandler;
import de.nak.iaa.sundenbock.exception.CustomAuthenticationEntryPoint;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.service.security.CustomSecurityService;
import de.nak.iaa.sundenbock.service.security.JwtService;
import de.nak.iaa.sundenbock.service.user.UserDetailsServiceImpl;
import de.nak.iaa.sundenbock.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        CustomSecurityService.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        UserDetailsServiceImpl.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomSecurityService customSecurityService;

    private UserDetailDTO testUserDetailDTO;

    @BeforeEach
    void setUp() {
        testUserDetailDTO = new UserDetailDTO(
                1L, "testuser", "Test", "User", "test@example.com",
                null, null, true, Set.of("ROLE_USER"), Set.of()
        );
    }

    // --- Security Tests (Anonymous) ---

    @Test
    @DisplayName("GET /api/v1/users should return 401 for anonymous user")
    @WithAnonymousUser
    void getAllUsers_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // --- Security Tests (Forbidden) ---

    @Test
    @DisplayName("GET /api/v1/users should return 403 for user without USER_MANAGE")
    @WithMockUser(username = "testuser", authorities = {"ROLE_DEVELOPER"})
    void getAllUsers_shouldReturn403_forUserWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("POST /api/v1/users/create should return 403 for user without USER_MANAGE")
    @WithMockUser(username = "testuser", authorities = {"ROLE_DEVELOPER"})
    void createUser_shouldReturn403_forUserWithoutPermission() throws Exception {
        CreateUserDTO createRequest = new CreateUserDTO(
                "newuser", "New", "User", "new@example.com", "password123", null
        );

        mockMvc.perform(post("/api/v1/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    // --- Security Tests (Self-Access) ---

    @Test
    @DisplayName("GET /api/v1/users/{username}/details should return 200 for user accessing own data")
    @WithMockUser(username = "testuser", authorities = {"ROLE_DEVELOPER"})
    void getDetailedUser_shouldReturn200_forSelf() throws Exception {
        when(customSecurityService.canAccessUser(eq("testuser"), any())).thenReturn(true);
        when(userService.getDetailedUserByUsername("testuser")).thenReturn(testUserDetailDTO);

        mockMvc.perform(get("/api/v1/users/testuser/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{username}/details should return 403 for user accessing other's data")
    @WithMockUser(username = "testuser", authorities = {"ROLE_DEVELOPER"})
    void getDetailedUser_shouldReturn403_forOtherUser() throws Exception {
        when(customSecurityService.canAccessUser(eq("otheruser"), any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/users/otheruser/details"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }


    // --- GET Endpoints (Admin) ---

    @Test
    @DisplayName("GET /api/v1/users should return user list for admin")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void getAllUsers_shouldReturnUserList_forAdmin() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users/{username} should return 404 when user not found")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void getUserByUsername_shouldReturn404_whenUserNotFound() throws Exception {
        when(customSecurityService.canAccessUser(eq("unknown"), any())).thenReturn(true);
        when(userService.getUserByUsername("unknown")).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{username} should return 400 for invalid username")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void getUserByUsername_shouldReturn400_forInvalidUsername() throws Exception {

        when(customSecurityService.canAccessUser(eq("a"), any())).thenReturn(true);

        mockMvc.perform(get("/api/v1/users/a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // --- POST/PUT/DELETE Endpoints (Admin) ---

    @Test
    @DisplayName("POST /api/v1/users/create should create user and return 201")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void createUser_shouldCreateUser_andReturn201() throws Exception {
        CreateUserDTO createRequest = new CreateUserDTO(
                "newuser", "New", "User", "new@example.com", "password123", null
        );

        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(testUserDetailDTO);

        mockMvc.perform(post("/api/v1/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/v1/users/create should return 400 on invalid data")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void createUser_shouldReturn400_onInvalidData() throws Exception {
        CreateUserDTO badRequest = new CreateUserDTO(
                "a", "", "", "not-an-email", "123", null
        );

        mockMvc.perform(post("/api/v1/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/update should update user and return 200")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void updateUser_shouldUpdateUser_andReturn200() throws Exception {
        UpdateUserDTO updateRequest = new UpdateUserDTO("NewFirst", "NewLast", "new@email.com", false);

        when(userService.updateUser(eq("testuser"), any(UpdateUserDTO.class))).thenReturn(testUserDetailDTO);

        mockMvc.perform(put("/api/v1/users/testuser/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/update should return 400 on invalid data")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void updateUser_shouldReturn400_onInvalidData() throws Exception {
        UpdateUserDTO badRequest = new UpdateUserDTO(null, null, "not-an-email", null);

        mockMvc.perform(put("/api/v1/users/testuser/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{username}/delete should delete user and return 200")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void deleteUser_shouldDeleteUser_andReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/users/testuser/delete")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{username}/delete should return 404 if user not found")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void deleteUser_shouldReturn404_ifUserNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(userService).deleteUserByUsername("unknown");

        mockMvc.perform(delete("/api/v1/users/unknown/delete")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/reset-password should return 200")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void adminResetPassword_shouldReturn200() throws Exception {
        AdminResetPasswordDTO request = new AdminResetPasswordDTO("newPassword123");

        mockMvc.perform(put("/api/v1/users/testuser/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/reset-password should return 400 for invalid password")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void adminResetPassword_shouldReturn400_forInvalidPassword() throws Exception {
        AdminResetPasswordDTO request = new AdminResetPasswordDTO("123");

        mockMvc.perform(put("/api/v1/users/testuser/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/roles/{roleName} should return 200")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void assignRole_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/v1/users/testuser/roles/ROLE_ADMIN")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{username}/roles/{roleName} should return 200")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void removeRole_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/users/testuser/roles/ROLE_ADMIN")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/permissions/{permissionName} should return 200")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void assignPermission_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/v1/users/testuser/permissions/USER_MANAGE")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{username}/permissions/{permissionName} should return 200")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void removePermission_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/users/testuser/permissions/USER_MANAGE")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // --- Validation Tests for Path Variables ---

    @Test
    @DisplayName("GET /api/v1/users/{username}/details should return 400 for invalid username")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void getDetailedUser_shouldReturn400_forInvalidUsername() throws Exception {
        when(customSecurityService.canAccessUser(eq("a"), any())).thenReturn(true);
        mockMvc.perform(get("/api/v1/users/a/details"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/update should return 400 for invalid username")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void updateUser_shouldReturn400_forInvalidUsername() throws Exception {
        UpdateUserDTO updateRequest = new UpdateUserDTO("NewFirst", "NewLast", "new@email.com", false);

        mockMvc.perform(put("/api/v1/users/a/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{username}/delete should return 400 for invalid username")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void deleteUser_shouldReturn400_forInvalidUsername() throws Exception {
        mockMvc.perform(delete("/api/v1/users/a/delete")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/reset-password should return 400 for invalid username")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void adminResetPassword_shouldReturn400_forInvalidUsername() throws Exception {
        AdminResetPasswordDTO request = new AdminResetPasswordDTO("newPassword123");

        mockMvc.perform(put("/api/v1/users/a/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{username}/roles/{roleName} should return 400 for invalid username")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void assignRole_shouldReturn400_forInvalidUsername() throws Exception {
        mockMvc.perform(put("/api/v1/users/a/roles/ROLE_ADMIN")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{username}/roles/{roleName} should return 400 for invalid username")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void removeRole_shouldReturn400_forInvalidUsername() throws Exception {
        mockMvc.perform(delete("/api/v1/users/a/roles/ROLE_ADMIN")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // --- Custom Exception Handling Tests ---

    @Test
    @DisplayName("POST /api/v1/users/create should return 409 for DuplicateResourceException")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void createUser_shouldReturn409_forDuplicateResource() throws Exception {
        CreateUserDTO createRequest = new CreateUserDTO(
                "newuser", "New", "User", "new@example.com", "password123", null
        );

        when(userService.createUser(any(CreateUserDTO.class)))
                .thenThrow(new de.nak.iaa.sundenbock.exception.DuplicateResourceException("Username exists"));

        mockMvc.perform(post("/api/v1/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{username}/delete should return 409 for SelfActionException")
    @WithMockUser(username = "admin", authorities = {"USER_MANAGE"})
    void deleteUser_shouldReturn409_forSelfAction() throws Exception {
        doThrow(new de.nak.iaa.sundenbock.exception.SelfActionException("Cannot delete self"))
                .when(userService).deleteUserByUsername("admin");

        mockMvc.perform(delete("/api/v1/users/admin/delete")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}