package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.config.JwtAuthFilter;
import de.nak.iaa.sundenbock.config.SecurityConfig;
import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.NavItemDTO;
import de.nak.iaa.sundenbock.exception.CustomAccessDeniedHandler;
import de.nak.iaa.sundenbock.exception.CustomAuthenticationEntryPoint;
import de.nak.iaa.sundenbock.service.navigation.NavigationRegistry;
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
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NavigationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        CustomSecurityService.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        UserDetailsServiceImpl.class
})
class NavigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NavigationRegistry navigationRegistry;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CustomSecurityService customSecurityService;

    // --- Security Tests ---

    @Test
    @DisplayName("GET /api/v1/ui/navigation should return 401 for anonymous user")
    @WithAnonymousUser
    void getNavItems_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/ui/navigation"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // --- GET Endpoint (Authenticated) ---

    @Test
    @DisplayName("GET /api/v1/ui/navigation should return nav items for authenticated user")
    @WithMockUser(username = "testuser", authorities = {"ROLE_DEVELOPER"})
    void getNavItems_shouldReturnNavItems_forAuthenticatedUser() throws Exception {
        List<NavItemDTO> navItems = List.of(
                new NavItemDTO("Dashboard", "/dashboard", "Home", Set.of()),
                new NavItemDTO("Tickets", "/tickets", "Ticket", Set.of("ROLE_DEVELOPNORMAL"))
        );

        when(navigationRegistry.getForPermissions(any())).thenReturn(navItems);

        mockMvc.perform(get("/api/v1/ui/navigation"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].label").value("Dashboard"));
    }
}