package de.nak.iaa.sundenbock.service.navigation;

import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.NavItemDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Spring component that builds and maintains a registry of UI navigation items.
 * <p>
 * This class is initialized once when the Spring {@link ApplicationContext} starts.
 * It eagerly scans the context for all beans annotated with {@link NavItem}. For each
 * discovered bean, it constructs a {@link NavItemDTO} that represents a link
 * in the application's navigation menu.
 *
 * <h3>Core Responsibilities:</h3>
 * <ul>
 * <li><b>Discovery:</b> Finds all navigation-related components (beans with {@link NavItem})
 * at application startup.</li>
 * <li><b>Permission Aggregation:</b> Gathers security requirements (permissions/roles)
 * for each item. It intelligently merges permissions defined directly in the
 * {@link NavItem} annotation with those programmatically extracted from other security
 * annotations on the component (via {@code RoleExtractor}).</li>
 * <li><b>Filtering & Caching:</b> Provides high-performance, cached methods to retrieve
 * either the complete navigation list ({@link #getAll()}) or a list filtered
 * according to a user's specific set of permissions ({@link #getForPermissions(Collection)}).</li>
 * </ul>
 * <p>
 * The resulting list of all navigation items is immutable and sorted alphabetically
 * by label.
 */
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

    /**
     * Returns the full navigation tree (cached).
     * @return immutable list of all navigation items
     */
    @Cacheable("nav:all")
    public List<NavItemDTO> getAll() {
        return allItems;
    }

    /**
     * Returns navigation items visible for the given set of permissions (cached per key).
     *
     * @param permissions caller permissions; used to filter required permissions
     * @return immutable list of allowed navigation items
     */
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
