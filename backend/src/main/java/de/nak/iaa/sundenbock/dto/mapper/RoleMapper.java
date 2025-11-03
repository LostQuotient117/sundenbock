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
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", expression = "java(permissionsToStrings(role.getPermissions()))")
    RoleDTO toRoleDTO(Role role);

    default Set<String> permissionsToStrings(Set<Permission> permissions) {
        if (permissions == null) {
            return null;
        }
        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
