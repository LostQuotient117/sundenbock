package de.nak.iaa.sundenbock.service.navigation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleExtractorTest {

    // --- Mock Controllers for Testing ---

    @RestController
    private static class ControllerWithHasAuthorityMethod {
        @PreAuthorize("hasAuthority('USER_MANAGE')")
        public void testMethod() {}
    }

    @RestController
    private static class ControllerWithHasRoleMethod {
        @PreAuthorize("hasRole('ADMIN')")
        public void testMethod() {}
    }

    @RestController
    private static class ControllerWithHasAnyAuthorityMethod {
        @PreAuthorize("hasAnyAuthority('USER_MANAGE', 'ROLE_MANAGE')")
        public void testMethod() {}
    }

    @RestController
    private static class ControllerWithHasAnyRoleMethod {
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public void testMethod() {}
    }

    @RestController
    @PreAuthorize("hasAuthority('CLASS_MANAGE')")
    private static class ControllerWithPreAuthorizeClass {
        public void testMethod() {}
    }

    @RestController
    @Secured("ROLE_CLASS_SECURED")
    private static class ControllerWithSecuredClass {
        public void testMethod() {}
    }

    @RestController
    private static class ControllerWithSecuredMethod {
        @Secured("ROLE_METHOD_SECURED")
        public void testMethod() {}
    }

    @RestController
    private static class ControllerWithMultipleMethods {
        @PreAuthorize("hasAuthority('PERM_A')")
        public void testMethod1() {}

        @Secured("ROLE_PERM_B")
        public void testMethod2() {}

        @PreAuthorize("hasAuthority('PERM_A')")
        public void testMethod3() {}
    }

    @RestController
    @PreAuthorize("hasAuthority('CLASS_PERM')")
    @Secured("ROLE_CLASS_SECURED")
    private static class ControllerWithMixedClassAndMethods {
        @PreAuthorize("hasAuthority('METHOD_PERM_A')")
        public void testMethod1() {}

        @Secured("ROLE_METHOD_PERM_B")
        public void testMethod2() {}
    }

    @RestController
    private static class ControllerWithComplexSpel {
        @GetMapping("/{username}")
        @PreAuthorize("isAuthenticated() and #username == authentication.name")
        public void testMethod(@PathVariable("username") String username) {}
    }

    @RestController
    private static class ControllerWithNoSecurity {
        @GetMapping("/test")
        public void testMethod() {}
    }

    // --- Test Cases ---

    @Test
    @DisplayName("should extract from hasAuthority on method")
    void shouldExtractFromHasAuthorityMethod() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithHasAuthorityMethod.class);
        assertThat(permissions).containsExactly("USER_MANAGE");
    }

    @Test
    @DisplayName("should extract from hasRole on method")
    void shouldExtractFromHasRoleMethod() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithHasRoleMethod.class);
        assertThat(permissions).containsExactly("ADMIN");
    }

    @Test
    @DisplayName("should extract from hasAnyAuthority on method")
    void shouldExtractFromHasAnyAuthorityMethod() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithHasAnyAuthorityMethod.class);
        assertThat(permissions).containsExactlyInAnyOrder("USER_MANAGE", "ROLE_MANAGE");
    }

    @Test
    @DisplayName("should extract from hasAnyRole on method")
    void shouldExtractFromHasAnyRoleMethod() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithHasAnyRoleMethod.class);
        assertThat(permissions).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("should extract from PreAuthorize on class")
    void shouldExtractFromPreAuthorizeClass() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithPreAuthorizeClass.class);
        assertThat(permissions).containsExactly("CLASS_MANAGE");
    }

    @Test
    @DisplayName("should extract from Secured on class and normalize")
    void shouldExtractFromSecuredClass() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithSecuredClass.class);
        assertThat(permissions).containsExactly("CLASS_SECURED");
    }

    @Test
    @DisplayName("should extract from Secured on method and normalize")
    void shouldExtractFromSecuredMethod() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithSecuredMethod.class);
        assertThat(permissions).containsExactly("METHOD_SECURED");
    }

    @Test
    @DisplayName("should merge permissions from multiple methods and remove duplicates")
    void shouldMergePermissionsFromMultipleMethods() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithMultipleMethods.class);
        assertThat(permissions).containsExactlyInAnyOrder("PERM_A", "PERM_B");
    }

    @Test
    @DisplayName("should merge permissions from class and methods")
    void shouldMergePermissionsFromClassAndMethods() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithMixedClassAndMethods.class);
        assertThat(permissions).containsExactlyInAnyOrder("CLASS_PERM", "CLASS_SECURED", "METHOD_PERM_A", "METHOD_PERM_B");
    }

    @Test
    @DisplayName("should return empty set for complex SpEL")
    void shouldReturnEmptySetForComplexSpel() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithComplexSpel.class);
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("should return empty set for no security annotation")
    void shouldReturnEmptySetForNoSecurity() {
        Set<String> permissions = RoleExtractor.extractRequiredPermissions(ControllerWithNoSecurity.class);
        assertThat(permissions).isEmpty();
    }
}