package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.auth.AuthenticationRequest;
import de.nak.iaa.sundenbock.dto.auth.AuthenticationResponse;
import de.nak.iaa.sundenbock.dto.auth.ChangePasswordRequest;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDetailDTO;
import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.service.AuthenticationService;
import de.nak.iaa.sundenbock.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for user authentication and registration endpoints.
 * Exposes public endpoints for user self-registration and login (authentication).
 */
@NavItem(label = "Authentication", path = "/auth", icon = "auth")
@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthenticationController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    /**
     * Registers a new user via the public registration endpoint.
     *
     * @param request the registration DTO containing username, email and password
     * @return a {@link ResponseEntity} with an {@link AuthenticationResponse} containing the issued JWT
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody CreateUserDTO request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    /**
     * Authenticates a user and returns a JWT on success.
     *
     * @param request the authentication request DTO containing username and password
     * @return a {@link ResponseEntity} with an {@link AuthenticationResponse} containing the issued JWT
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    /**
     * Retrieves the details of the currently authenticated user.
     *
     * @return A {@link UserDetailDTO} for the logged-in user.
     */
    @GetMapping("/me")
    public UserDetailDTO getMyDetails() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getDetailedUserByUsername(currentUsername);
    }

    /**
     * Allows the currently authenticated user to change their own password.
     *
     * @param request the change password request containing old and new passwords
     */
    @PutMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
    }
}
