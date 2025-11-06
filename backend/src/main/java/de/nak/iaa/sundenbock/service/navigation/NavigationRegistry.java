package de.nak.iaa.sundenbock.service.navigation;

import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.dto.navigationDTO.NavItemDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class NavigationRegistry {

    private final List<NavItemDTO> allItems;

    public NavigationRegistry(ApplicationContext ctx) {
        this.allItems = ctx.getBeansWithAnnotation(NavItem.class)
                .values()
                .stream()
                .map(Object::getClass)
                .map(ClassUtils::getUserClass)
                .map(this::buildNavItemDTO)
                .sorted(Comparator.comparing(NavItemDTO::label))
                .toList();
    }

    private NavItemDTO buildNavItemDTO(Class<?> beanClass) {
        NavItem navItemAnnotation = beanClass.getAnnotation(NavItem.class);

        Set<String> permissionsFromAnn = Arrays.stream(navItemAnnotation.permissions())
                .map(RoleExtractor::trimString)
                .collect(Collectors.toSet());

        Set<String> permissionsFromSecurity = RoleExtractor.extractRequiredPermissions(beanClass);

        Set<String> mergedPermissions = new HashSet<>();
        mergedPermissions.addAll(permissionsFromAnn);
        mergedPermissions.addAll(permissionsFromSecurity);

        return new NavItemDTO(
                navItemAnnotation.label(),
                navItemAnnotation.path(),
                navItemAnnotation.icon(),
                Set.copyOf(mergedPermissions)
        );
    }

    @Cacheable("nav:all")
    public List<NavItemDTO> getAll() {
        return allItems;
    }

    @Cacheable(value = "nav:byPermissions", key = "T(java.lang.String).join(',', #permissions)")
    public List<NavItemDTO> getForPermissions(Collection<String> permissions) {
        Set<String> userPermissions = Set.copyOf(permissions);
        return allItems.stream()
                .filter(i -> i.requiredPermissions().isEmpty() ||
                        hasCommonElements(i.requiredPermissions(), userPermissions))
                .toList();
    }

    private static boolean hasCommonElements(Set<String> requiredPermissions, Set<String> userPermissions) {
        for (String requiredPerm : requiredPermissions) {
            if (userPermissions.contains(requiredPerm)) {
                return true;
            }
        }
        return false;
    }
}
