package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.user.Permission;
import de.nak.iaa.sundenbock.model.user.Role;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Order(1) // Ensures this runner executes first
public class LoadBaselineSecurityData implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public LoadBaselineSecurityData(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
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
        Role userRole = createRoleIfNotFound("ROLE_USER", Set.of(
                ticketCreate, ticketReadOwn, commentCreate
        ));

        Role devRole = createRoleIfNotFound("ROLE_DEVELOPER", Set.of(
                projectRead, ticketCreate, ticketReadAll, ticketUpdate,
                commentCreate, commentUpdate, commentDelete
        ));

        Role managerRole = createRoleIfNotFound("ROLE_PROJECT_MANAGER", Set.of(
                projectCreate, projectRead, projectUpdate, projectDelete,
                ticketCreate, ticketReadAll, ticketUpdate, ticketDelete,
                ticketAssign, ticketChangeStatus,
                commentCreate, commentUpdate, commentDelete
        ));

        Role adminRole = createRoleIfNotFound("ROLE_ADMIN", Set.of(
                projectCreate, projectRead, projectUpdate, projectDelete,
                ticketCreate, ticketReadAll, ticketUpdate, ticketDelete,
                ticketAssign, ticketChangeStatus,
                commentCreate, commentUpdate, commentDelete,
                userManage, roleManage
        ));

        Role viewerRole = createRoleIfNotFound("ROLE_VIEWER", Set.of(
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

    private Role createRoleIfNotFound(String name, Set<Permission> permissions) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setPermissions(permissions);
            return roleRepository.save(role);
        });
    }

}
