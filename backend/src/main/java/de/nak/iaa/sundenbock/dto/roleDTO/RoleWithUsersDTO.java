package de.nak.iaa.sundenbock.dto.roleDTO;

import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import java.util.List;
import java.util.Set;

/**
 * DTO representing a Role and the list of users assigned to it.
 */
public record RoleWithUsersDTO(
        Long id,
        String name,
        Set<String> permissions,
        List<UserDTO> users
) {}
