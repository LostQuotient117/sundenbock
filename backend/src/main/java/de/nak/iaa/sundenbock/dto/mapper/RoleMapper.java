package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for converting between {@link Role} entities and
 * {@link de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO} objects.
 * <p>
 * This interface is managed as a Spring component and provides the
 * necessary logic to map complex nested objects, such as converting
 * the {@code Set<Permission>} into a {@code Set<String>} of permission names.
 *
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * Converts a {@link Role} persistence entity into its {@link RoleDTO}
     * data transfer object representation.
     * <p>
     * It uses the custom mapping logic defined in
     * {@link #permissionsToStrings(Set)} to convert the nested
     * {@link Permission} entities into a simple set of strings.
     *
     * @param role The source {@link Role} entity to convert.
     * @return The corresponding {@link RoleDTO}.
     */
    @Mapping(target = "permissions", expression = "java(permissionsToStrings(role.getPermissions()))")
    RoleDTO toRoleDTO(Role role);

    /**
     * A default helper method used by MapStruct (via an {@code @Mapping} expression)
     * to transform a set of {@link Permission} entities into a set of their
     * string names.
     * <p>
     * This method is null-safe; it returns {@code null} if the input set is
     * {@code null}.
     *
     * @param permissions The collection of {@link Permission} entities.
     * @return A {@link Set} containing the names ({@code String}) of the
     * permissions, or {@code null}.
     */
    default Set<String> permissionsToStrings(Set<Permission> permissions) {
        if (permissions == null) {
            return null;
        }
        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
