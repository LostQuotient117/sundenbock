package de.nak.iaa.sundenbock.navigation;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class NavigationController {

    private final NavigationRegistry registry;

    public NavigationController(NavigationRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/api/v1/ui/navigation")
    public List<NavItemDTO> getNav(Authentication auth) {
        var roles = extractRoles(auth.getAuthorities());
        return registry.getForRoles(roles);
    }

    private Set<String> extractRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .collect(Collectors.toSet());
    }
}
