package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.auth.AuthenticationRequest;
import de.nak.iaa.sundenbock.dto.auth.AuthenticationResponse;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller für die Authentifizierung und Registrierung von Benutzern.
 * Stellt Endpunkte für die Registrierung neuer Benutzer und die Authentifizierung bestehender Benutzer bereit.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    // public user self-registration
    /**
     * Registriert einen neuen Benutzer im System.
     * @param request Das Registrierungsanforderungs-DTO, das Benutzername, E-Mail und Passwort enthält.
     * @return Eine ResponseEntity, die ein AuthenticationResponse-DTO mit dem JWT-Token enthält.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody CreateUserDTO request) {
        return ResponseEntity.ok(service.register(request));
    }

    // basically login
    /**
     * Authentifiziert einen Benutzer und gibt bei Erfolg einen JWT-Token zurück.
     * @param request Das Authentifizierungsanforderungs-DTO, das Benutzername und Passwort enthält.
     * @return Eine ResponseEntity, die ein AuthenticationResponse-DTO mit dem JWT-Token enthält.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

}
