package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.model.permission.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDTO toPermissionDTO(Permission permission);
}
