package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Data loader for creating sample users for testing and demonstration.
 * <p>
 * This runner populates the database with a set of default users
 * (dev, manager, admin) and assigns them the roles created by
 * {@link LoadBaselineSecurityData}.
 * It should run *after* the baseline security data is loaded.
 */
@Component
class LoadSampleUsersData {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;

    public LoadSampleUsersData(UserRepository userRepository, RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder, PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public void run() {

        Role devRole = roleRepository.findByName(Role.DEVELOPER).orElseThrow();
        Role managerRole = roleRepository.findByName("ROLE_PROJECT_MANAGER").orElseThrow();
        Role adminRole = roleRepository.findByName(Role.ADMIN).orElseThrow();
        Role userRole = roleRepository.findByName(Role.USER).orElseThrow();
        Role viewerRole = roleRepository.findByName("ROLE_VIEWER").orElseThrow();

        Permission ticketAssignPerm = permissionRepository.findById("TICKET_ASSIGN")
                .orElseThrow(() -> new RuntimeException("Permission TICKET_ASSIGN not found"));

        // ========== Sample Users ==========
        createUserIfNotFound("OG-Developer", "Peter", "Zwegat","dev@test.com", "password420", Set.of(devRole), Set.of());
        createUserIfNotFound("MainManager", "Justus", "Jonas","manager@test.com", "password047", Set.of(managerRole), Set.of());
        createUserIfNotFound("Super-admin-666", "Klaus", "Kleber","admin@test.com", "password357", Set.of(adminRole), Set.of());
        createUserIfNotFound("poorTester", "Bruce", "Wayne","tester@test.com", "password789", Set.of(userRole), Set.of());
        createUserIfNotFound("ObserverViewer", "Mia", "Khalifa","viewer@test.com", "password456", Set.of(viewerRole), Set.of());

        // ========== Sample Users with custom permissions ==========
        // Has "ROLE_USER" and additionally permission "TICKET_ASSIGN"
        createUserIfNotFound("very-special-specialist", "Geralt", "von Riva", "specialist@test.com", "password123",
                Set.of(userRole), Set.of(ticketAssignPerm));

        System.out.println("Test user data (permissions, roles, users) loaded!");
    }

    private void createUserIfNotFound(String username, String firstName, String lastName, String email, String password, Set<Role> roles, Set<Permission> permissions) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(roles);
            user.setPermissions(permissions);
            userRepository.save(user);
        }
    }
}
