package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDetailDTO;
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

    User toEntity(UserDTO userDTO); //needed for TicketMapper

    @Mapping(target = "permissions", expression = "java(mapAllPermissions(user))")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    UserDetailDTO toUserDetailDTO(User user);

    /**
     * Maps a Set of Role entities to a Set of role names (Strings).
     * @param roles The set of {@link Role} entities.
     * @return A Set of role name Strings.
     */
    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    /**
     * Aggregates all permissions for a user into a single Set of strings.
     * This includes both permissions assigned directly to the user and
     * all permissions inherited from the user's roles.
     *
     * @param user The {@link User} entity.
     * @return A distinct Set of all permission name Strings.
     */
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
