package de.nak.iaa.sundenbock.service.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CustomSecurityServiceTest {

    @InjectMocks
    private CustomSecurityService customSecurityService;

    private Authentication mockAuthentication(String username, String... authorities) {
        Authentication auth = mock(Authentication.class);
        Mockito.lenient().when(auth.getName()).thenReturn(username);
        Set<SimpleGrantedAuthority> authoritySet = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        Mockito.lenient().when(auth.getAuthorities()).thenAnswer(invocation -> authoritySet);
        return auth;
    }

    // --- canAccessUser Tests ---

    @Test
    @DisplayName("canAccessUser should return true if user has USER_MANAGE")
    void canAccessUser_shouldReturnTrue_ifUserIsAdmin() {
        Authentication adminAuth = mockAuthentication("admin", "USER_MANAGE");
        boolean canAccess = customSecurityService.canAccessUser("otherUser", adminAuth);
        assertThat(canAccess).isTrue();
    }

    @Test
    @DisplayName("canAccessUser should return true if user is accessing own data")
    void canAccessUser_shouldReturnTrue_ifAccessingSelf() {
        Authentication userAuth = mockAuthentication("testuser", "ROLE_DEVELOPER");
        boolean canAccess = customSecurityService.canAccessUser("testuser", userAuth);
        assertThat(canAccess).isTrue();
    }

    @Test
    @DisplayName("canAccessUser should return false if user accesses other's data without permission")
    void canAccessUser_shouldReturnFalse_ifAccessingOtherWithoutPermission() {
        Authentication userAuth = mockAuthentication("testuser", "ROLE_DEVELOPER");
        boolean canAccess = customSecurityService.canAccessUser("otherUser", userAuth);
        assertThat(canAccess).isFalse();
    }

    @Test
    @DisplayName("canAccessUser should return false if authentication is null")
    void canAccessUser_shouldReturnFalse_ifAuthenticationIsNull() {
        boolean canAccess = customSecurityService.canAccessUser("testuser", null);
        assertThat(canAccess).isFalse();
    }
}