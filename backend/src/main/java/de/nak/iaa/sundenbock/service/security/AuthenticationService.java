package de.nak.iaa.sundenbock.service.security;

import de.nak.iaa.sundenbock.dto.auth.AuthenticationRequest;
import de.nak.iaa.sundenbock.dto.auth.AuthenticationResponse;
import de.nak.iaa.sundenbock.dto.auth.ChangePasswordRequest;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.UserDisabledException;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that encapsulates business logic for user registration and authentication.
 * <p>
 * Responsible for creating users, assigning default roles during registration,
 * authenticating credentials and issuing JWT tokens.
 */
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder, JwtService jwtService,
                                 AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Registers a new user via the public registration endpoint.
     * Creates a new user record, assigns the default "ROLE_DEVELOPER" role,
     * saves the user, and generates a JWT.
     *
     * @param request The registration request containing user data.
     * @return An AuthenticationResponse containing the generated JWT.
     */
    public AuthenticationResponse register(CreateUserDTO request) {

        CreateUserDTO trimmedRequest = new CreateUserDTO(
                request.username().trim(),
                request.firstName().trim(),
                request.lastName().trim(),
                request.email().trim(),
                request.password(),
                request.roles()
        );

        Role defaultRole = roleRepository.findByName("ROLE_DEVELOPER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        if (userRepository.findByUsername(trimmedRequest.username()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + trimmedRequest.username());
        }

        if (userRepository.existsByEmail(trimmedRequest.email())) {
            throw new DuplicateResourceException("Email already in use: " + trimmedRequest.email());
        }

        User user = new User();
        user.setUsername(trimmedRequest.username());
        user.setEmail(trimmedRequest.email());
        user.setFirstName(trimmedRequest.firstName());
        user.setLastName(trimmedRequest.lastName());
        user.setPassword(passwordEncoder.encode(trimmedRequest.password()));
        user.setRoles(Set.of(defaultRole));
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(trimmedRequest.username());
        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthenticationResponse(jwtToken);
    }

    /**
     * Authenticates a user based on their username and password.
     * On successful authentication, a new JWT is generated.
     *
     * @param request The authentication request with username and password.
     * @return An AuthenticationResponse containing the generated JWT.
     * @throws BadCredentialsException if the credentials are invalid.
     * @throws UserDisabledException if the user account is disabled.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        String trimmedUsername = request.username().trim();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(trimmedUsername, request.password())
            );
        } catch (DisabledException e) {
            throw new UserDisabledException("Your account is disabled. Please contact an administrator.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(trimmedUsername);
        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthenticationResponse(jwtToken);
    }

    /**
     * Changes the password of the currently authenticated user.
     * <p>
     * Verifies the provided old password, encodes the new password, and persists the change.
     * </p>
     *
     * @param request contains the old and new password
     * @throws BadCredentialsException if the old password does not match
     * @throws ResourceNotFoundException if the currently authenticated user cannot be found
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found in database"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
