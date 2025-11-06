package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.navigationDTO.NavItemDTO;
import de.nak.iaa.sundenbock.service.navigation.NavigationRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ui/navigation")
public class NavigationController {

    private final NavigationRegistry registry;

    public NavigationController(NavigationRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    public List<NavItemDTO> getNavItems(Authentication authentication) {
        Collection<String> permissions = getPermissionsFromAuth(authentication);
        return registry.getForPermissions(permissions);
    }

    private Collection<String> getPermissionsFromAuth(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .map(Authentication::getAuthorities)
                .orElse(Set.of())
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
