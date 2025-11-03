package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.model.permission.Permission;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between {@link Permission} entities and
 * {@link de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO} objects.
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDTO toPermissionDTO(Permission permission);
}
