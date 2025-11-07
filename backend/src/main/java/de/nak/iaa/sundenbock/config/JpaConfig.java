package de.nak.iaa.sundenbock.config;

import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * JPA configuration that enables auditing.
 * <p>
 * Provides an {@link AuditorAware} bean that returns the current authenticated {@link User}
 * or a fallback 'system' user when no authentication is available (e.g. at startup).
 * <p>
 * The implementation avoids triggering database queries for the current user when the principal
 * is already the {@link User} entity, preventing circular save/autoflush issues inside transactions.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JpaConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Supplies the current auditor for Spring Data JPA auditing.
     * <p>
     * - If a fully authenticated {@link User} instance is present in the SecurityContext, it is returned directly
     *   (avoids extra DB calls during transactional save operations).
     * - Otherwise, it tries to find an existing 'system' or 'admin' user or creates a disabled 'system' user as fallback.
     *
     * @return an {@link Optional} containing the current {@link User} acting as auditor
     */
    @Bean
    public AuditorAware<User> auditorAware() {
        return () -> {
            var auth =  SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()){
                Object principal = auth.getPrincipal();
                if (principal instanceof User) {
                    return Optional.of((User) principal);
                }

            }

            return userRepository.findByUsername("system")
                    .or(() -> userRepository.findByUsername("admin"))
                    .or(() -> {
                        User systemUser = new User();
                        systemUser.setUsername("system");
                        systemUser.setEmail("system@local");
                        systemUser.setPassword(passwordEncoder.encode("systemPassword-LE"));
                        systemUser.setFirstName("System");
                        systemUser.setLastName("User");
                        systemUser.setEnabled(false);
                        User savedSystemUser = userRepository.save(systemUser);
                        return Optional.of(savedSystemUser);
                    });

        };
    }
}
