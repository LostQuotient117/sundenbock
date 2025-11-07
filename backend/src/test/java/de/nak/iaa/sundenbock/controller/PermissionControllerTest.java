package de.nak.iaa.sundenbock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nak.iaa.sundenbock.config.JwtAuthFilter;
import de.nak.iaa.sundenbock.config.SecurityConfig;
import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.exception.CustomAccessDeniedHandler;
import de.nak.iaa.sundenbock.exception.CustomAuthenticationEntryPoint;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.service.PermissionService;
import de.nak.iaa.sundenbock.service.security.CustomSecurityService;
import de.nak.iaa.sundenbock.service.security.JwtService;
import de.nak.iaa.sundenbock.service.user.UserDetailsServiceImpl;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PermissionController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        CustomSecurityService.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        UserDetailsServiceImpl.class
})
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CustomSecurityService customSecurityService;

    // --- Security Tests ---

    @Test
    @DisplayName("GET /api/v1/permissions should return 401 for anonymous user")
    @WithAnonymousUser
    void getAllPermissions_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GET /api/v1/permissions should return 403 for user without ROLE_MANAGE")
    @WithMockUser(username = "testuser", authorities = {"ROLE_DEVELOPER"})
    void getAllPermissions_shouldReturn403_forUserWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // --- GET Endpoint (Admin) ---

    @Test
    @DisplayName("GET /api/v1/permissions should return list for admin")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void getAllPermissions_shouldReturnList_forAdmin() throws Exception {
        when(permissionService.getAllPermissions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk());
    }

    // --- POST Endpoint (Admin) ---

    @Test
    @DisplayName("POST /api/v1/permissions/create should create permission and return 200")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void createPermission_shouldCreatePermission_andReturn200() throws Exception {
        PermissionDTO createRequest = new PermissionDTO("NEW_PERMISSION");

        when(permissionService.createPermission(any(PermissionDTO.class))).thenReturn(createRequest);

        mockMvc.perform(post("/api/v1/permissions/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NEW_PERMISSION"));
    }

    @Test
    @DisplayName("POST /api/v1/permissions/create should return 400 for invalid data")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void createPermission_shouldReturn400_forInvalidData() throws Exception {
        PermissionDTO badRequest = new PermissionDTO("");

        mockMvc.perform(post("/api/v1/permissions/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // --- DELETE Endpoint (Admin) ---

    @Test
    @DisplayName("DELETE /api/v1/permissions/{permissionName}/delete should return 200")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void deletePermission_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/permissions/TEST_PERM/delete")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/permissions/{permissionName}/delete should return 404 for RNF")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void deletePermission_shouldReturn404_forRNF() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(permissionService).deletePermission("UNKNOWN_PERM");

        mockMvc.perform(delete("/api/v1/permissions/UNKNOWN_PERM/delete")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/permissions/{permissionName}/delete should return 400 for invalid path param")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void deletePermission_shouldReturn400_forInvalidPathParam() throws Exception {
        mockMvc.perform(delete("/api/v1/permissions/ /delete")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // --- Custom Exception Handling Tests ---

    @Test
    @DisplayName("DELETE /api/v1/permissions/{permissionName}/delete should return 409 for PermissionInUseException")
    @WithMockUser(username = "admin", authorities = {"ROLE_MANAGE"})
    void deletePermission_shouldReturn409_forPermissionInUse() throws Exception {
        doThrow(new de.nak.iaa.sundenbock.exception.PermissionInUseException("Permission is in use"))
                .when(permissionService).deletePermission("TEST_PERM");

        mockMvc.perform(delete("/api/v1/permissions/TEST_PERM/delete")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}