package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.auth.AuthenticationRequest;
import de.nak.iaa.sundenbock.dto.auth.AuthenticationResponse;
import de.nak.iaa.sundenbock.dto.auth.ChangePasswordRequest;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.service.AuthenticationService;
import de.nak.iaa.sundenbock.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for user authentication and registration endpoints.
 * <p>
 * Exposes public endpoints for user self-registration and login (authentication).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService service;
    private final UserService userService;

    public AuthenticationController(AuthenticationService service, UserService userService) {
        this.service = service;
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
        return ResponseEntity.ok(service.register(request));
    }

    /**
     * Authenticates a user and returns a JWT on success.
     *
     * @param request the authentication request DTO containing username and password
     * @return a {@link ResponseEntity} with an {@link AuthenticationResponse} containing the issued JWT
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    /**
     * Allows the currently authenticated user to change their own password.
     *
     * @param request the change password request containing old and new passwords
     */
    @PutMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
    }

}
