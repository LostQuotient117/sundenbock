package de.nak.iaa.sundenbock.navigation;

//import jakarta.persistence.Cacheable;
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
                .map(this::toDescriptor)
                .sorted(Comparator.comparing(NavItemDTO::label))
                .toList();
    }

    private NavItemDTO toDescriptor(Class<?> beanClass) {
        var ann = beanClass.getAnnotation(NavItem.class);
        var rolesFromAnn = Arrays.stream(ann.roles())
                .map(RoleExtractor::normalizeRole)
                .collect(Collectors.toSet());
        var rolesFromSecurity = RoleExtractor.extractRequiredRoles(beanClass);

        var merged = new HashSet<String>();
        merged.addAll(rolesFromAnn);
        merged.addAll(rolesFromSecurity);

        return new NavItemDTO(
                ann.label(),
                ann.path(),
                ann.icon(),
                Set.copyOf(merged)
        );
    }

    @Cacheable("nav:all")
    public List<NavItemDTO> getAll() {
        return allItems;
    }

    @Cacheable(value = "nav:byRoles", key = "T(java.lang.String).join(',', #roles)")
    public List<NavItemDTO> getForRoles(Collection<String> roles) {
        var set = roles.stream().map(RoleExtractor::normalizeRole).collect(Collectors.toSet());
        return allItems.stream()
                .filter(i -> i.requiredRoles().isEmpty() || intersects(i.requiredRoles(), set))
                .toList();
    }

    private static boolean intersects(Set<String> a, Set<String> b) {
        for (var x : a) if (b.contains(x)) return true;
        return false;
    }
}
