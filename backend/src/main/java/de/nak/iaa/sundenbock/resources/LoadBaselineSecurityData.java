package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Data loader for creating essential security baselines (Permissions and Roles).
 * <p>
 * This runner populates the database with all necessary permissions
 * (e.g., "PROJECT_CREATE", "USER_MANAGE") and the default roles
 * (e.g., "ROLE_ADMIN", "ROLE_DEVELOPER") that depend on them.
 * It ensures that the application has a consistent set of authorities
 * on startup. It is designed to run before {@link LoadSampleUsersData}.
 */
@Component
public class LoadBaselineSecurityData{

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public LoadBaselineSecurityData(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }
    @Transactional
    public void run() {
        // ========== Base Permissions ==========
        // Project Permissions
        Permission projectCreate = createPermissionIfNotFound("PROJECT_CREATE");
        Permission projectRead = createPermissionIfNotFound("PROJECT_READ");
        Permission projectUpdate = createPermissionIfNotFound("PROJECT_UPDATE");
        Permission projectDelete = createPermissionIfNotFound("PROJECT_DELETE");

        // Ticket Permissions
        Permission ticketCreate = createPermissionIfNotFound("TICKET_CREATE");
        Permission ticketReadOwn = createPermissionIfNotFound("TICKET_READ_OWN");
        Permission ticketReadAll = createPermissionIfNotFound("TICKET_READ_ALL");
        Permission ticketUpdate = createPermissionIfNotFound("TICKET_UPDATE");
        Permission ticketDelete = createPermissionIfNotFound("TICKET_DELETE");
        Permission ticketAssign = createPermissionIfNotFound("TICKET_ASSIGN");
        Permission ticketChangeStatus = createPermissionIfNotFound("TICKET_CHANGE_STATUS");

        // Comment Permissions
        Permission commentCreate = createPermissionIfNotFound("COMMENT_CREATE");
        Permission commentUpdate = createPermissionIfNotFound("COMMENT_UPDATE");
        Permission commentDelete = createPermissionIfNotFound("COMMENT_DELETE");

        // User Management Permissions
        Permission userManage = createPermissionIfNotFound("USER_MANAGE");
        Permission roleManage = createPermissionIfNotFound("ROLE_MANAGE");

        // ========== Base Roles ==========
        createRoleIfNotFound("ROLE_USER", Set.of(
                ticketCreate, ticketReadOwn, commentCreate
        ));

        createRoleIfNotFound("ROLE_DEVELOPER", Set.of(
                projectRead, ticketCreate, ticketReadAll, ticketUpdate,
                commentCreate, commentUpdate, commentDelete
        ));

        createRoleIfNotFound("ROLE_PROJECT_MANAGER", Set.of(
                projectCreate, projectRead, projectUpdate, projectDelete,
                ticketCreate, ticketReadAll, ticketUpdate, ticketDelete,
                ticketAssign, ticketChangeStatus,
                commentCreate, commentUpdate, commentDelete
        ));

        createRoleIfNotFound("ROLE_ADMIN", Set.of(
                projectCreate, projectRead, projectUpdate, projectDelete,
                ticketCreate, ticketReadAll, ticketUpdate, ticketDelete,
                ticketAssign, ticketChangeStatus,
                commentCreate, commentUpdate, commentDelete,
                userManage, roleManage
        ));

        createRoleIfNotFound("ROLE_VIEWER", Set.of(
                projectRead, ticketReadAll
        ));

        System.out.println("Baseline permissions and roles loaded!");
    }

    private Permission createPermissionIfNotFound(String name) {
        return permissionRepository.findById(name).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setName(name);
            return permissionRepository.save(permission);
        });
    }

    private void createRoleIfNotFound(String name, Set<Permission> permissions) {
        roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setPermissions(permissions);
            return roleRepository.save(role);
        });
    }

}
