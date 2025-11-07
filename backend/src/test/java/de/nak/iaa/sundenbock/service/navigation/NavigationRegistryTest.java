package de.nak.iaa.sundenbock.service.navigation;

import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.NavItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NavigationRegistryTest {

    @Mock
    private ApplicationContext mockApplicationContext;

    private NavigationRegistry navigationRegistry;

    // --- Mock Controller Classes ---

    @NavItem(label = "Users", path = "/users", icon = "User")
    @RestController
    @RequestMapping("/api/v1/users")
    private static class MockUserController {
        @PreAuthorize("hasAuthority('USER_MANAGE')")
        public void getAllUsers() {}
    }

    @NavItem(label = "Roles", path = "/roles", icon = "Role")
    @RestController
    @RequestMapping("/api/v1/roles")
    private static class MockRoleController {
        @PreAuthorize("hasAuthority('ROLE_MANAGE')")
        public void getAllRoles() {}
    }

    @NavItem(label = "Dashboard", path = "/dashboard", icon = "Home")
    @RestController
    @RequestMapping("/api/v1/dashboard")
    private static class MockDashboardController {
        @PreAuthorize("isAuthenticated()")
        public void getDashboard() {}
    }

    @BeforeEach
    void setUp() {
        Map<String, Object> mockBeans = Map.of(
                "mockUserController", new MockUserController(),
                "mockRoleController", new MockRoleController(),
                "mockDashboardController", new MockDashboardController()
        );

        when(mockApplicationContext.getBeansWithAnnotation(NavItem.class)).thenReturn(mockBeans);

        navigationRegistry = new NavigationRegistry(mockApplicationContext);
    }

    // --- Test Cases ---

    @Test
    @DisplayName("Registry should correctly parse NavItem and PreAuthorize annotations")
    void registry_shouldParseAnnotationsCorrectly() {
        List<NavItemDTO> allItems = navigationRegistry.getAll();

        assertThat(allItems).hasSize(3);

        NavItemDTO userItem = allItems.stream().filter(i -> i.label().equals("Users")).findFirst().orElse(null);
        NavItemDTO roleItem = allItems.stream().filter(i -> i.label().equals("Roles")).findFirst().orElse(null);
        NavItemDTO dashboardItem = allItems.stream().filter(i -> i.label().equals("Dashboard")).findFirst().orElse(null);

        assertThat(userItem).isNotNull();
        assertThat(userItem.path()).isEqualTo("/users");
        assertThat(userItem.requiredPermissions()).containsExactly("USER_MANAGE");

        assertThat(roleItem).isNotNull();
        assertThat(roleItem.path()).isEqualTo("/roles");
        assertThat(roleItem.requiredPermissions()).containsExactly("ROLE_MANAGE");

        assertThat(dashboardItem).isNotNull();
        assertThat(dashboardItem.path()).isEqualTo("/dashboard");
        assertThat(dashboardItem.requiredPermissions()).isEmpty();
    }

    @Test
    @DisplayName("getForPermissions should filter items correctly for admin")
    void getForPermissions_shouldFilterForAdmin() {
        Set<String> adminPermissions = Set.of("USER_MANAGE", "ROLE_MANAGE");

        List<NavItemDTO> visibleItems = navigationRegistry.getForPermissions(adminPermissions);

        assertThat(visibleItems).hasSize(3);
        assertThat(visibleItems.stream().map(NavItemDTO::label))
                .containsExactlyInAnyOrder("Dashboard", "Users", "Roles");
    }

    @Test
    @DisplayName("getForPermissions should filter items correctly for basic user")
    void getForPermissions_shouldFilterForBasicUser() {
        Set<String> userPermissions = Set.of("SOME_OTHER_PERM");

        List<NavItemDTO> visibleItems = navigationRegistry.getForPermissions(userPermissions);

        assertThat(visibleItems).hasSize(1);
        assertThat(visibleItems.getFirst().label()).isEqualTo("Dashboard");
    }

    @Test
    @DisplayName("getForPermissions should return all items if one permission matches")
    void getForPermissions_shouldFilterForPartialPermissions() {
        Set<String> userManagePermissions = Set.of("USER_MANAGE");

        List<NavItemDTO> visibleItems = navigationRegistry.getForPermissions(userManagePermissions);

        assertThat(visibleItems).hasSize(2);
        assertThat(visibleItems.stream().map(NavItemDTO::label))
                .containsExactlyInAnyOrder("Dashboard", "Users");
    }

    @Test
    @DisplayName("getForPermissions should return only dashboard for empty permissions")
    void getForPermissions_shouldFilterForEmptyPermissions() {
        Set<String> noPermissions = Set.of();

        List<NavItemDTO> visibleItems = navigationRegistry.getForPermissions(noPermissions);

        assertThat(visibleItems).hasSize(1);
        assertThat(visibleItems.getFirst().label()).isEqualTo("Dashboard");
    }
}