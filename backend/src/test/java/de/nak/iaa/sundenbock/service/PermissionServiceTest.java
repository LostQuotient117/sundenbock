package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.mapper.PermissionMapper;
import de.nak.iaa.sundenbock.dto.permissionDTO.PermissionDTO;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.PermissionInUseException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission testPermission;
    private PermissionDTO testPermissionDTO;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setName("TEST_PERMISSION");

        testPermissionDTO = new PermissionDTO("TEST_PERMISSION");
    }

    // --- Get All Permissions ---

    @Test
    @DisplayName("getAllPermissions should return list of PermissionDTOs")
    void getAllPermissions_shouldReturnPermissionDTOList() {
        when(permissionRepository.findAll()).thenReturn(List.of(testPermission));
        when(permissionMapper.toPermissionDTO(testPermission)).thenReturn(testPermissionDTO);

        List<PermissionDTO> result = permissionService.getAllPermissions();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("TEST_PERMISSION");
        verify(permissionRepository, times(1)).findAll();
        verify(permissionMapper, times(1)).toPermissionDTO(testPermission);
    }

    // --- Create Permission ---

    @Test
    @DisplayName("createPermission should create and return PermissionDTO")
    void createPermission_shouldCreateAndReturnPermissionDTO() {
        when(permissionRepository.existsById("TEST_PERMISSION")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);
        when(permissionMapper.toPermissionDTO(testPermission)).thenReturn(testPermissionDTO);

        PermissionDTO result = permissionService.createPermission(testPermissionDTO);

        assertThat(result.name()).isEqualTo("TEST_PERMISSION");
        verify(permissionRepository).save(argThat(permission ->
                permission.getName().equals("TEST_PERMISSION")
        ));
    }

    @Test
    @DisplayName("createPermission should throw DuplicateResourceException when name exists")
    void createPermission_shouldThrowDuplicateResourceException_whenNameExists() {
        when(permissionRepository.existsById("TEST_PERMISSION")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.createPermission(testPermissionDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Permission already exists: TEST_PERMISSION");
    }

    // --- Delete Permission ---

    @Test
    @DisplayName("deletePermission should delete when not in use")
    void deletePermission_shouldDelete_whenNotInUse() {
        when(permissionRepository.existsById("TEST_PERMISSION")).thenReturn(true);
        when(roleRepository.countByPermissions_Name("TEST_PERMISSION")).thenReturn(0L);
        when(userRepository.countByPermissions_Name("TEST_PERMISSION")).thenReturn(0L);

        permissionService.deletePermission("TEST_PERMISSION");

        verify(permissionRepository, times(1)).deleteById("TEST_PERMISSION");
    }

    @Test
    @DisplayName("deletePermission should throw RNF when permission not found")
    void deletePermission_shouldThrowResourceNotFoundException_whenPermissionNotFound() {
        when(permissionRepository.existsById("TEST_PERMISSION")).thenReturn(false);

        assertThatThrownBy(() -> permissionService.deletePermission("TEST_PERMISSION"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Permission not found: TEST_PERMISSION");
    }

    @Test
    @DisplayName("deletePermission should throw PermissionInUseException when used by roles")
    void deletePermission_shouldThrowPermissionInUseException_whenUsedByRoles() {
        when(permissionRepository.existsById("TEST_PERMISSION")).thenReturn(true);
        when(roleRepository.countByPermissions_Name("TEST_PERMISSION")).thenReturn(2L);

        assertThatThrownBy(() -> permissionService.deletePermission("TEST_PERMISSION"))
                .isInstanceOf(PermissionInUseException.class)
                .hasMessageContaining("Cannot delete permission. It is still used by 2 role(s).");

        verify(permissionRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("deletePermission should throw PermissionInUseException when used by users")
    void deletePermission_shouldThrowPermissionInUseException_whenUsedByUsers() {
        when(permissionRepository.existsById("TEST_PERMISSION")).thenReturn(true);
        when(roleRepository.countByPermissions_Name("TEST_PERMISSION")).thenReturn(0L);
        when(userRepository.countByPermissions_Name("TEST_PERMISSION")).thenReturn(1L);

        assertThatThrownBy(() -> permissionService.deletePermission("TEST_PERMISSION"))
                .isInstanceOf(PermissionInUseException.class)
                .hasMessageContaining("Cannot delete permission. It is still directly assigned to 1 user(s).");

        verify(permissionRepository, never()).deleteById(anyString());
    }
}