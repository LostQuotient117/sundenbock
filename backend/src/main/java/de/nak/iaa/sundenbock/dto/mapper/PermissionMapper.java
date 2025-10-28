package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.PermissionDTO;
import de.nak.iaa.sundenbock.model.user.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDTO toPermissionDTO(Permission permission);
}
