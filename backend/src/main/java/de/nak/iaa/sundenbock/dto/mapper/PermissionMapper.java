package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.model.permission.Permission;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between {@link Permission} entities and
 * {@link de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO} objects.
 * <p>
 * This is a straightforward mapper, configured as a Spring component,
 * that performs a direct field-to-field mapping between the entity and the DTO.
 *
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper {
    /**
     * Converts a {@link Permission} persistence entity into its corresponding
     * {@link PermissionDTO} data transfer object.
     * <p>
     * MapStruct will automatically handle the mapping of fields with
     * identical names (e.g., {@code name}, {@code description}).
     *
     * @param permission The source {@link Permission} entity to convert.
     * @return The resulting {@link PermissionDTO}.
     */
    PermissionDTO toPermissionDTO(Permission permission);
}
