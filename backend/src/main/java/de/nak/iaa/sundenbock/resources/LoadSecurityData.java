package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.user.Permission;
import de.nak.iaa.sundenbock.model.user.Role;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.PermissionRepository;
import de.nak.iaa.sundenbock.repository.RoleRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "spring.datasource.url", havingValue = "jdbc:h2:mem:mydb") // only used in h2 db, so i hope no crash when using prod
public class LoadSecurityData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public LoadSecurityData(UserRepository userRepository, RoleRepository roleRepository,
                            PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Creates and saves all test permissions
        Permission ticketRead = new Permission();
        ticketRead.setName("TICKET_READ");
        permissionRepository.save(ticketRead);

        Permission ticketCreate = new Permission();
        ticketCreate.setName("TICKET_CREATE");
        permissionRepository.save(ticketCreate);

        Permission ticketEdit = new Permission();
        ticketEdit.setName("TICKET_EDIT");
        permissionRepository.save(ticketEdit);

        Permission userManage = new Permission();
        userManage.setName("USER_MANAGE");
        permissionRepository.save(userManage);

        // Creates and saves test roles with their permissions
        Role devRole = new Role();
        devRole.setName("ROLE_DEVELOPER");
        devRole.setPermissions(Set.of(ticketRead, ticketCreate, ticketEdit));
        roleRepository.save(devRole);

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminRole.setPermissions(Set.of(ticketRead, ticketCreate, ticketEdit, userManage));
        roleRepository.save(adminRole);

        // Creates and saves test users with their roles
        if (userRepository.findByUsername("dev").isEmpty()) {
            User devUser = new User();
            devUser.setUsername("dev");
            devUser.setEmail("dev@test.com");
            devUser.setPassword(passwordEncoder.encode("password")); // PASSWORT VERSCHLÜSSELN
            devUser.setRoles(Set.of(devRole));
            userRepository.save(devUser);
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@test.com");
            adminUser.setPassword(passwordEncoder.encode("password")); // PASSWORT VERSCHLÜSSELN
            adminUser.setRoles(Set.of(adminRole));
            userRepository.save(adminUser);
        }

        System.out.println("Test user data (permissions, roles, users) loaded!");
    }

}
