package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.NavItemDTO;
import de.nak.iaa.sundenbock.service.navigation.NavigationRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST endpoint exposing the UI navigation tree filtered by the caller's permissions.
 */
@RestController
@RequestMapping("/api/v1/ui/navigation")
public class NavigationController {

    private final NavigationRegistry registry;

    public NavigationController(NavigationRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns the navigation items visible to the authenticated user.
     *
     * @param authentication Spring Security authentication (used to derive permissions)
     * @return a list of navigation items allowed for the user
     */
    @GetMapping
    public List<NavItemDTO> getNavItems(Authentication authentication) {
        Collection<String> permissions = getPermissionsFromAuth(authentication);
        return registry.getForPermissions(permissions);
    }

    /**
     * Extracts permission strings from the current authentication.
     */
    private Collection<String> getPermissionsFromAuth(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .map(Authentication::getAuthorities)
                .orElse(Set.of())
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
