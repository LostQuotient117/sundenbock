package de.nak.iaa.sundenbock.service.security;

import de.nak.iaa.sundenbock.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails mockUserDetails;
    private JwtProperties mockProperties;

    @BeforeEach
    void setUp() {
        String testSecret = "N1NlY3JldEtleUZvclRlc3RpbmdQdXJwb3NlczEyMzQ1Njc4OQ==";
        long testExpiration = 3600000; // 1 hour

        mockProperties = mock(JwtProperties.class);
        when(mockProperties.secretKey()).thenReturn(testSecret);
        when(mockProperties.expiration()).thenReturn(testExpiration);

        jwtService = new JwtService(mockProperties);

        mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn("testuser");
        Set<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("USER_MANAGE")
        );
        when(mockUserDetails.getAuthorities()).thenAnswer(invocation -> authorities);
    }

    // --- Token Generation and Validation ---

    @Test
    @DisplayName("generateToken should create a token with correct username (subject)")
    void generateToken_shouldContainCorrectUsername() {
        String token = jwtService.generateToken(mockUserDetails);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("generateToken should create a token with custom 'auth' claim")
    void generateToken_shouldContainCustomAuthClaim() throws Exception {
        String token = jwtService.generateToken(mockUserDetails);

        Method extractAllClaimsMethod = JwtService.class.getDeclaredMethod("extractAllClaims", String.class);
        extractAllClaimsMethod.setAccessible(true);
        Claims claims = (Claims) extractAllClaimsMethod.invoke(jwtService, token);

        String authClaim = claims.get("auth", String.class);

        assertThat(authClaim).isNotNull();
        assertThat(authClaim).contains("ROLE_USER");
        assertThat(authClaim).contains("USER_MANAGE");
    }

    @Test
    @DisplayName("isTokenValid should return true for a valid, non-expired token")
    void isTokenValid_shouldReturnTrue_forValidToken() {
        String token = jwtService.generateToken(mockUserDetails);
        boolean isValid = jwtService.isTokenValid(token, mockUserDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid should return false for a token with wrong user")
    void isTokenValid_shouldReturnFalse_forWrongUser() {
        String token = jwtService.generateToken(mockUserDetails);

        UserDetails otherUserDetails = mock(UserDetails.class);
        when(otherUserDetails.getUsername()).thenReturn("otherUser");

        boolean isValid = jwtService.isTokenValid(token, otherUserDetails);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid should return false for an expired token")
    void isTokenValid_shouldReturnFalse_forExpiredToken() throws Exception {
        when(mockProperties.expiration()).thenReturn(1L); // 1ms expiration
        JwtService expiredJwtService = new JwtService(mockProperties);

        String token = expiredJwtService.generateToken(mockUserDetails);

        Thread.sleep(2);

        boolean isValid = expiredJwtService.isTokenValid(token, mockUserDetails);
        assertThat(isValid).isFalse();
    }
}