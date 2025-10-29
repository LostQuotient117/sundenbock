package de.nak.iaa.sundenbock.resources;

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
public class LoadSampleUsersData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public LoadSampleUsersData(UserRepository userRepository, RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        Role devRole = roleRepository.findByName("ROLE_DEVELOPER").orElseThrow();
        Role managerRole = roleRepository.findByName("ROLE_PROJECT_MANAGER").orElseThrow();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

        // ========== Sample Users ==========
        createUserIfNotFound("dev", "dev@test.com", "password", Set.of(devRole));
        createUserIfNotFound("manager", "manager@test.com", "password", Set.of(managerRole));
        createUserIfNotFound("admin", "admin@test.com", "password", Set.of(adminRole));

        System.out.println("Test user data (permissions, roles, users) loaded!");
    }

    private void createUserIfNotFound(String username, String email, String password, Set<Role> roles) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(roles);
            userRepository.save(user);
        }
    }
}
