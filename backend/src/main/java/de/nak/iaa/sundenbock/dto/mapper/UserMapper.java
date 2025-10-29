package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.dto.UserDetailDTO;
import de.nak.iaa.sundenbock.model.user.Permission;
import de.nak.iaa.sundenbock.model.user.Role;
import de.nak.iaa.sundenbock.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "permissions", expression = "java(mapPermissions(user.getPermissions()))")
    UserDetailDTO toUserDetailDTO(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    default Set<String> mapPermissions(Set<Permission> permissions) {
        if (permissions == null) return null;
        return permissions.stream().map(Permission::getName).collect(Collectors.toSet());
    }

    /**
     * Es werden nur die nicht-ignorierten Felder (wie z.B. 'id' oder 'username') aus dem DTO in das User-Objekt gemappt.
     */
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    User toUser(UserDTO userDTO);
}
