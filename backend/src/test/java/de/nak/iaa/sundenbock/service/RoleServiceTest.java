package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.mapper.RoleMapper;
import de.nak.iaa.sundenbock.dto.roleDTO.CreateRoleDTO;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.RoleInUseException;
import de.nak.iaa.sundenbock.exception.SelfActionException;
import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    private Role adminRole;
    private Permission userManagePermission;
    private RoleDTO adminRoleDTO;

    @BeforeEach
    void setUp() {
        userManagePermission = new Permission();
        userManagePermission.setName("USER_MANAGE");

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ROLE_ADMIN");
        adminRole.setPermissions(Set.of(userManagePermission));

        adminRoleDTO = new RoleDTO(1L, "ROLE_ADMIN", Set.of("USER_MANAGE"));
    }

    // --- Get All Roles ---

    @Test
    @DisplayName("getAllRoles should return a list of RoleDTOs")
    void getAllRoles_shouldReturnRoleDTOList() {
        when(roleRepository.findAll()).thenReturn(List.of(adminRole));
        when(roleMapper.toRoleDTO(adminRole)).thenReturn(adminRoleDTO);

        List<RoleDTO> result = roleService.getAllRoles();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("ROLE_ADMIN");
        verify(roleRepository, times(1)).findAll();
        verify(roleMapper, times(1)).toRoleDTO(adminRole);
    }

    // --- Create Role ---

    @Test
    @DisplayName("createRole should create and return RoleDTO")
    void createRole_shouldCreateAndReturnRoleDTO() {
        CreateRoleDTO request = new CreateRoleDTO("NEW_ROLE", Set.of("USER_MANAGE"));
        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.empty());
        when(permissionRepository.findById("USER_MANAGE")).thenReturn(Optional.of(userManagePermission));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleMapper.toRoleDTO(any(Role.class))).thenReturn(new RoleDTO(2L, "NEW_ROLE", Set.of("USER_MANAGE")));

        RoleDTO result = roleService.createRole(request);

        assertThat(result.name()).isEqualTo("NEW_ROLE");
        verify(roleRepository).save(argThat(role ->
                role.getName().equals("NEW_ROLE") &&
                        role.getPermissions().contains(userManagePermission)
        ));
    }

    @Test
    @DisplayName("createRole should throw DuplicateResourceException when role name exists")
    void createRole_shouldThrowDuplicateResourceException_whenRoleNameExists() {
        CreateRoleDTO request = new CreateRoleDTO("ROLE_ADMIN", Set.of());
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Role already exists: ROLE_ADMIN");
    }

    @Test
    @DisplayName("createRole should throw ResourceNotFoundException when permission not found")
    void createRole_shouldThrowResourceNotFoundException_whenPermissionNotFound() {
        CreateRoleDTO request = new CreateRoleDTO("NEW_ROLE", Set.of("FAKE_PERMISSION"));
        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.empty());
        when(permissionRepository.findById("FAKE_PERMISSION")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Permission not found: FAKE_PERMISSION");
    }

    // --- Update Role Permissions ---

    @Test
    @DisplayName("updateRolePermissions should update permissions")
    void updateRolePermissions_shouldUpdatePermissions() {
        Permission newPermission = new Permission();
        newPermission.setName("ROLE_MANAGE");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(permissionRepository.findById("ROLE_MANAGE")).thenReturn(Optional.of(newPermission));

        roleService.updateRolePermissions(1L, Set.of("ROLE_MANAGE"));

        verify(roleRepository).save(argThat(role ->
                role.getPermissions().contains(newPermission) &&
                        !role.getPermissions().contains(userManagePermission) &&
                        role.getPermissions().size() == 1
        ));
    }

    // --- Update Role Permissions (Unhappy Path) ---

    @Test
    @DisplayName("updateRolePermissions should throw RNF when permission not found")
    void updateRolePermissions_shouldThrowResourceNotFoundException_whenPermissionNotFound() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(permissionRepository.findById("FAKE_PERMISSION")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRolePermissions(1L, Set.of("FAKE_PERMISSION")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Permission not found: FAKE_PERMISSION");
    }

    @Test
    @DisplayName("updateRolePermissions should throw RNF when role not found")
    void updateRolePermissions_shouldThrowResourceNotFoundException_whenRoleNotFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRolePermissions(99L, Set.of()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");
    }

    // --- Delete Role ---

    @Test
    @DisplayName("deleteRole should delete role when not in use")
    void deleteRole_shouldDeleteRole_whenNotInUse() {
        Role customRole = new Role();
        customRole.setId(5L);
        customRole.setName("CUSTOM_ROLE");

        when(roleRepository.findById(5L)).thenReturn(Optional.of(customRole));
        when(userRepository.countByRoles_Id(5L)).thenReturn(0L);

        roleService.deleteRole(5L);

        verify(roleRepository, times(1)).delete(customRole);
    }

    @Test
    @DisplayName("deleteRole should throw SelfActionException when deleting ROLE_ADMIN")
    void deleteRole_shouldThrowSelfActionException_whenDeletingRoleAdmin() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));

        assertThatThrownBy(() -> roleService.deleteRole(1L))
                .isInstanceOf(SelfActionException.class)
                .hasMessageContaining("Cannot delete core system role: ROLE_ADMIN");
    }

    @Test
    @DisplayName("deleteRole should throw SelfActionException when deleting ROLE_USER")
    void deleteRole_shouldThrowSelfActionException_whenDeletingRoleUser() {
        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setName("ROLE_USER");
        when(roleRepository.findById(2L)).thenReturn(Optional.of(userRole));

        assertThatThrownBy(() -> roleService.deleteRole(2L))
                .isInstanceOf(SelfActionException.class)
                .hasMessageContaining("Cannot delete core system role: ROLE_USER");
    }

    @Test
    @DisplayName("deleteRole should throw RoleInUseException when role is assigned to users")
    void deleteRole_shouldThrowRoleInUseException_whenRoleIsInUse() {
        Role customRole = new Role();
        customRole.setId(5L);
        customRole.setName("CUSTOM_ROLE");

        when(roleRepository.findById(5L)).thenReturn(Optional.of(customRole));
        when(userRepository.countByRoles_Id(5L)).thenReturn(3L);

        assertThatThrownBy(() -> roleService.deleteRole(5L))
                .isInstanceOf(RoleInUseException.class)
                .hasMessageContaining("Cannot delete role. It is still assigned to 3 user(s).");
    }

    @Test
    @DisplayName("deleteRole should throw RNF when role not found")
    void deleteRole_shouldThrowResourceNotFoundException_whenRoleNotFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.deleteRole(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found with id: 99");
    }
}