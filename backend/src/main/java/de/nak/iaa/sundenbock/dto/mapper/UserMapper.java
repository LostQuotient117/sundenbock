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

/**
 * A MapStruct mapper interface responsible for converting between the {@link User}
 * persistence entity and its various Data Transfer Object (DTO) representations,
 * such as {@link UserDTO} and {@link UserDetailDTO}.
 * <p>
 * This mapper is configured as a Spring component and can be injected into
 * services.
 *
 * <h3>Key Logic: Permission Aggregation</h3>
 * A critical feature of this mapper is its ability to aggregate permissions.
 * When mapping to {@link UserDetailDTO}, it uses the {@link #mapAllPermissions(User)}
 * method to create a comprehensive, flat set of permissions. This set includes
 * permissions assigned <strong>directly</strong> to the user as well as all permissions
 * <strong>inherited</strong> from the user's assigned roles.
 *
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a {@link User} entity to its basic {@link UserDTO} representation.
     * <p>
     * This mapping is typically used for overview lists or simple references
     * where detailed information (like roles, permissions, or full timestamps)
     * is not required.
     *
     * @param user The {@link User} entity to convert.
     * @return The corresponding {@link UserDTO}.
     */
    UserDTO toUserDTO(User user);

    /**
     * Converts a basic {@link UserDTO} back into a {@link User} entity.
     * <p>
     * <strong>Note:</strong> This method is primarily intended for use within
     * other mappers (e.g., {@code TicketMapper}) to re-associate entities
     * based on a simple DTO reference (e.g., setting a ticket's responsible person).
     * It does not map complex fields like roles, permissions, or passwords.
     *
     * @param userDTO The {@link UserDTO} to convert.
     * @return The corresponding {@link User} entity (likely with partial data).
     */
    User toEntity(UserDTO userDTO);

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
