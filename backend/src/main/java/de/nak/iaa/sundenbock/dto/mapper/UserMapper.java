package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.dto.UserDetailDTO;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    @Mapping(target = "permissions", expression = "java(mapAllPermissions(user))")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    UserDetailDTO toUserDetailDTO(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    default Set<String> mapAllPermissions(User user) {
        if (user == null) {
            return null;
        }
        Set<String> allPermissions = new HashSet<>();

        // Add direct permissions
        if (user.getPermissions() != null) {
            user.getPermissions().stream()
                    .map(Permission::getName)
                    .forEach(allPermissions::add);
        }

        // Add permissions from roles
        if (user.getRoles() != null) {
            user.getRoles().forEach(role ->
                    role.getPermissions().stream()
                            .map(Permission::getName)
                            .forEach(allPermissions::add)
            );
        }

        return allPermissions;
    }
}
