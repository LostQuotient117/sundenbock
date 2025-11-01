package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.dto.mapper.PermissionMapper;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toPermissionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        Permission permission = new Permission();
        permission.setName(permissionDTO.name());
        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toPermissionDTO(savedPermission);
    }
}
