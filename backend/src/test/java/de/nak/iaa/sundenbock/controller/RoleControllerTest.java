package de.nak.iaa.sundenbock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nak.iaa.sundenbock.config.JwtAuthFilter;
import de.nak.iaa.sundenbock.config.SecurityConfig;
import de.nak.iaa.sundenbock.dto.roleDTO.CreateRoleDTO;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleWithUsersDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.exception.CustomAccessDeniedHandler;
import de.nak.iaa.sundenbock.exception.CustomAuthenticationEntryPoint;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.service.RoleService;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        CustomSecurityService.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        UserDetailsServiceImpl.class
})
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CustomSecurityService customSecurityService;

    // --- Security Tests ---

    @Test
    @DisplayName("GET /api/v1/roles should return 401 for anonymous user")
    @WithAnonymousUser
    void getAllRoles_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GET /api/v1/roles should return 403 for user without ROLE_MANAGE")
    @WithMockUser(username = "testuser", authorities = {"ROLE_DEVELOPER"})
    void getAllRoles_shouldReturn403_forUserWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // --- GET Endpoint (Admin) ---

    @Test
    @DisplayName("GET /api/v1/roles should return role list for admin")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void getAllRoles_shouldReturnRoleList_forAdmin() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/roles/with-users should return 200 and full list for admin")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void getAllRolesWithUsers_shouldReturn200_forAdmin() throws Exception {
        UserDTO user1 = new UserDTO(1L, "user1", "Test", "User");
        RoleWithUsersDTO roleDTO = new RoleWithUsersDTO(
                1L, "ROLE_ADMIN", Set.of("USER_MANAGE"), List.of(user1)
        );
        when(roleService.getAllRolesWithUsers()).thenReturn(List.of(roleDTO));

        mockMvc.perform(get("/api/v1/roles/with-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[0].users[0].username").value("user1"));
    }

    @Test
    @DisplayName("GET /api/v1/roles/with-users should return 401 for anonymous")
    @WithAnonymousUser
    void getAllRolesWithUsers_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/roles/with-users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/roles/with-users should return 403 for user without ROLE_MANAGE or USER_MANAGE")
    @WithMockUser(authorities = "ROLE_DEVELOPER")
    void getAllRolesWithUsers_shouldReturn403_forForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/roles/with-users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/roles/with-users should return 200 for USER_MANAGE authority")
    @WithMockUser(authorities = "USER_MANAGE")
    void getAllRolesWithUsers_shouldReturn200_forUserManageAuth() throws Exception {
        when(roleService.getAllRolesWithUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles/with-users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/roles/{roleName}/users should return 400 for blank role name")
    @WithMockUser(authorities = "USER_MANAGE")
    void getUsersByRole_shouldReturn400_forBlankRoleName() throws Exception {
        mockMvc.perform(get("/api/v1/roles/ /users"))
                .andExpect(status().isBadRequest());
    }

    // --- POST Endpoint (Admin) ---

    @Test
    @DisplayName("POST /api/v1/roles should create role and return 200")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void createRole_shouldCreateRole_andReturn200() throws Exception {
        CreateRoleDTO createRequest = new CreateRoleDTO("NEW_ROLE", Set.of("PERM_A"));
        RoleDTO createdRole = new RoleDTO(1L, "NEW_ROLE", Set.of("PERM_A"));

        when(roleService.createRole(any(CreateRoleDTO.class))).thenReturn(createdRole);

        mockMvc.perform(post("/api/v1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NEW_ROLE"));
    }

    @Test
    @DisplayName("POST /api/v1/roles should return 400 for invalid data")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void createRole_shouldReturn400_forInvalidData() throws Exception {
        CreateRoleDTO badRequest = new CreateRoleDTO("", null);

        mockMvc.perform(post("/api/v1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // --- PUT Endpoint (Admin) ---

    @Test
    @DisplayName("PUT /api/v1/roles/{roleId}/update-permissions should return 200")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void updateRolePermissions_shouldReturn200() throws Exception {
        Set<String> permissions = Set.of("PERM_A", "PERM_B");

        mockMvc.perform(put("/api/v1/roles/1/update-permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{roleId}/update-permissions should return 404 for RNF")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void updateRolePermissions_shouldReturn404_forRNF() throws Exception {
        Set<String> permissions = Set.of("PERM_A");

        doThrow(new ResourceNotFoundException("Not found"))
                .when(roleService).updateRolePermissions(eq(99L), anySet());

        mockMvc.perform(put("/api/v1/roles/99/update-permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{roleId}/update-permissions should return 400 for invalid path param")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void updateRolePermissions_shouldReturn400_forInvalidPathParam() throws Exception {
        Set<String> permissions = Set.of("PERM_A");

        mockMvc.perform(put("/api/v1/roles/0/update-permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE Endpoint (Admin) ---

    @Test
    @DisplayName("DELETE /api/v1/roles/{roleId}/delete should return 200")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void deleteRole_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/1/delete")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/roles/{roleId}/delete should return 404 for RNF")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void deleteRole_shouldReturn404_forRNF() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(roleService).deleteRole(99L);

        mockMvc.perform(delete("/api/v1/roles/99/delete")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/roles/{roleId}/delete should return 400 for invalid path param")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void deleteRole_shouldReturn400_forInvalidPathParam() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/0/delete")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // --- Custom Exception Handling Tests ---

    @Test
    @DisplayName("DELETE /api/v1/roles/{roleId}/delete should return 409 for RoleInUseException")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void deleteRole_shouldReturn409_forRoleInUse() throws Exception {
        doThrow(new de.nak.iaa.sundenbock.exception.RoleInUseException("Role is in use"))
                .when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/api/v1/roles/1/delete")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}